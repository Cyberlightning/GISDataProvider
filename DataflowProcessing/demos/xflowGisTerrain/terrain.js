/*--------------------------------------------------*/
//
//  Xflow Terrain Generator(WebCL) 1.0
//  User can generate procedural terrain or use gis
//  data to create terrain tile.
//
// Jouni Mietola @ Cyberlightning Ltd.
//
/*--------------------------------------------------*/
(function () {
    "use strict";
    XML3D.debug.loglevel = 0;

    /**
     * Registering JS Operators
     *
     */

    Xflow.registerOperator("xflow.customgrid", {
        outputs: [
            {type: 'float3', name: 'position', customAlloc: true},
            {type: 'float3', name: 'normal', customAlloc: true},
            {type: 'float2', name: 'texcoord', customAlloc: true},
            {type: 'int', name: 'index', customAlloc: true}
        ],
        params: [
            {type: 'int', source: 'area', array: true},
            {type: 'texture', name: 'output', sizeof: 'input'}
        ],
        alloc: function (areas, area) {
            var s = area[0];
            areas['position'] = s * s;
            areas['normal'] = s * s;
            areas['texcoord'] = s * s;
            areas['index'] = (s - 1) * (s - 1) * 6;
        },
        evaluate: function (position, normal, texcoord, index, area, texture) {

            var s = area[0];
            var data = texture.data;
            console.log("area", s);
            console.log("data", data.length);

            // Create Positions
            for (var i = 0; i < position.length / 3; i++) {

                var offset = i * 3;
                position[offset] = (((i % s) / (s - 1)) - 0.5) * 2;
                position[offset + 1] = data[i*2]/100;
                position[offset + 2] = ((Math.floor(i / s) / (s - 1)) - 0.5) * 2;
            }

            // Create Normals
            for (var i = 0; i < normal.length / 3; i++) {
                var offset = i * 3;
                normal[offset] = 0;
                normal[offset + 1] = 1;
                normal[offset + 2] = 0;
            }
            // Create Texture Coordinates
            for (var i = 0; i < texcoord.length / 2; i++) {
                var offset = i * 2;
                texcoord[offset] = (i % s) / (s - 1);
                texcoord[offset + 1] = Math.floor(i / s) / (s - 1);
            }

            // Create Indices
            var length = (s - 1) * (s - 1);
            for (var i = 0; i < length; i++) {
                var offset = i * 6;
                var base = i + Math.floor(i / (s - 1));
                index[offset + 0] = base;
                index[offset + 1] = base + 1;
                index[offset + 2] = base + s;
                index[offset + 4] = base + s;
                index[offset + 3] = base + 1;
                index[offset + 5] = base + s + 1;
            }
        }
    });

    function floatToByte(f) {
        f = Math.max(0.0, Math.min(1.0, f));
        return Math.floor(f === 1.0 ? 255 : f * 256.0);
    }

    function byteToFloat(b) {
        b = Math.max(0, Math.min(255, b));
        return b === 255 ? 1.0 : b / 256.0;
    }

    Xflow.registerOperator("xflow.blurImage", {
        outputs: [
            {type: 'texture', name: 'output', sizeof: 'input'}
        ],
        params: [
            {type: 'texture', source: 'input'},
            {type: 'int', source: 'blurSize'}
        ],

        platform: Xflow.PLATFORM.JAVASCRIPT,

        evaluate: function (output, input, blurSize) {
            var s = input.data;
            var sLen = s.length;
            var d = output.data;
            var m = [0.05, 0.09, 0.12, 0.15, 0.16, 0.15, 0.12, 0.09, 0.05];
            var sumR = 0;
            var sumG = 0;
            var sumB = 0;
            var currentCoord = 0;
            var i, j;

            for (i = 0; i < sLen; i += 4) {
                for (j = 0; j < 5; j++) {
                    currentCoord = (i - (16 - j * 4) * blurSize[0]);
                    if (currentCoord >= 0 && currentCoord <= sLen) {
                        sumR += byteToFloat(s[currentCoord]) * m[j];
                        sumG += byteToFloat(s[currentCoord + 1]) * m[j];
                        sumB += byteToFloat(s[currentCoord + 2]) * m[j];
                    }
                }

                d[i] = floatToByte(sumR);
                d[i + 1] = floatToByte(sumG);
                d[i + 2] = floatToByte(sumB);
                d[i + 3] = 255;
                sumR = sumG = sumB = 0;
            }

            return true;
        }
    });

    /**
     * Registering WebCL Operators
     *
     */

    Xflow.registerOperator("xflow.clDeform", {
        outputs: [
            {type: 'float3', name: 'position'},
            {type: 'float3', name: 'normal'}
        ],
        params: [
            {type: 'float3', source: 'position' },
            {type: 'float3', source: 'normal'},
            {type: 'float', source: 'amplitude'},
            {type: 'float', source: 'phase'}

        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [
            "        __constant int P_MASK = 255;",
            "        __constant int P_SIZE = 256;",
            "        __constant int P[512] = {151,160,137,91,90,15,",
            "          131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,",
            "          190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,",
            "          88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,",
            "          77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,",
            "          102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,",
            "          135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,",
            "          5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,",
            "          223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,",
            "          129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,",
            "          251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,",
            "          49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,",
            "          138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180,",
            "          151,160,137,91,90,15,",
            "          131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,",
            "          190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,",
            "          88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,",
            "          77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,",
            "          102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,",
            "          135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,",
            "          5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,",
            "          223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,",
            "          129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,",
            "          251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,",
            "          49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,",
            "          138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180,",
            "          };",

            "        __constant int G_MASK = 15;",
            "        __constant int G_SIZE = 16;",
            "        __constant int G_VECSIZE = 4;",
            "        __constant float G[16*4] = {",
            "                 +1.0, +1.0, +0.0, 0.0 ,",
            "                 -1.0, +1.0, +0.0, 0.0 ,",
            "                 +1.0, -1.0, +0.0, 0.0 ,",
            "                 -1.0, -1.0, +0.0, 0.0 ,",
            "                 +1.0, +0.0, +1.0, 0.0 ,",
            "                 -1.0, +0.0, +1.0, 0.0 ,",
            "                 +1.0, +0.0, -1.0, 0.0 ,",
            "                 -1.0, +0.0, -1.0, 0.0 ,",
            "                 +0.0, +1.0, +1.0, 0.0 ,",
            "                 +0.0, -1.0, +1.0, 0.0 ,",
            "                 +0.0, +1.0, -1.0, 0.0 ,",
            "                 +0.0, -1.0, -1.0, 0.0 ,",
            "                 +1.0, +1.0, +0.0, 0.0 ,",
            "                 -1.0, +1.0, +0.0, 0.0 ,",
            "                 +0.0, -1.0, +1.0, 0.0 ,",
            "                 +0.0, -1.0, -1.0, 0.0",
            "        };",

            "        int mod(int x, int a)",
            "        {",
            "                int n = (x / a);",
            "                int v = v - n * a;",
            "                if ( v < 0 )",
            "                        v += a;",
            "                return v;",
            "        }",

            "        float mix1d(float a, float b, float t)",
            "        {",
            "                float ba = b - a;",
            "                float tba = t * ba;",
            "                float atba = a + tba;",
            "                return atba;",
            "        }",

            "        float2 mix2d(float2 a, float2 b, float t)",
            "        {",
            "                float2 ba = b - a;",
            "                float2 tba = t * ba;",
            "                float2 atba = a + tba;",
            "                return atba;",
            "        }",

            "        float4 mix3d(float4 a, float4 b, float t)",
            "        {",
            "                float4 ba = b - a;",
            "                float4 tba = t * ba;",
            "                float4 atba = a + tba;",
            "                return atba;",
            "        }",

            "        float smooth(float t)",
            "        {",
            "                return t*t*t*(t*(t*6.0f-15.0f)+10.0f);",
            "        }",

            "        int lattice3d(int4 i)",
            "        {",
            "                return P[i.x + P[i.y + P[i.z]]];",
            "        }",

            "        float gradient3d(int4 i, float4 v)",
            "        {",
            "                int index = (lattice3d(i) & G_MASK) * G_VECSIZE;",
            "                float4 g = (float4)(G[index + 0], G[index + 1], G[index + 2], 1.0f);",
            "                return dot(v, g);",
            "        }",

            "        float4 normalized(float4 v)",
            "        {",
            "                float d = sqrt(v.x * v.x + v.y * v.y + v.z * v.z);",
            "                d = d > 0.0f ? d : 1.0f;",
            "                float4 result = (float4)(v.x, v.y, v.z, 0.0f) / d;",
            "                result.w = 1.0f;",
            "                return result;",
            "        }",

            "        float gradient_noise3d(float4 position)",
            "        {",

            "                float4 p = position;",
            "                float4 pf = floor(p);",
            "                int4 ip = (int4)((int)pf.x, (int)pf.y, (int)pf.z, 0.0);",
            "                float4 fp = p - pf;",
            "                ip &= P_MASK;",

            "                int4 I000 = (int4)(0, 0, 0, 0);",
            "                int4 I001 = (int4)(0, 0, 1, 0);",
            "                int4 I010 = (int4)(0, 1, 0, 0);",
            "                int4 I011 = (int4)(0, 1, 1, 0);",
            "                int4 I100 = (int4)(1, 0, 0, 0);",
            "                int4 I101 = (int4)(1, 0, 1, 0);",
            "                int4 I110 = (int4)(1, 1, 0, 0);",
            "                int4 I111 = (int4)(1, 1, 1, 0);",

            "                float4 F000 = (float4)(0.0f, 0.0f, 0.0f, 0.0f);",
            "                float4 F001 = (float4)(0.0f, 0.0f, 1.0f, 0.0f);",
            "                float4 F010 = (float4)(0.0f, 1.0f, 0.0f, 0.0f);",
            "                float4 F011 = (float4)(0.0f, 1.0f, 1.0f, 0.0f);",
            "                float4 F100 = (float4)(1.0f, 0.0f, 0.0f, 0.0f);",
            "                float4 F101 = (float4)(1.0f, 0.0f, 1.0f, 0.0f);",
            "                float4 F110 = (float4)(1.0f, 1.0f, 0.0f, 0.0f);",
            "                float4 F111 = (float4)(1.0f, 1.0f, 1.0f, 0.0f);",

            "                float n000 = gradient3d(ip + I000, fp - F000);",
            "                float n001 = gradient3d(ip + I001, fp - F001);",

            "                float n010 = gradient3d(ip + I010, fp - F010);",
            "                float n011 = gradient3d(ip + I011, fp - F011);",

            "                float n100 = gradient3d(ip + I100, fp - F100);",
            "                float n101 = gradient3d(ip + I101, fp - F101);",

            "                float n110 = gradient3d(ip + I110, fp - F110);",
            "                float n111 = gradient3d(ip + I111, fp - F111);",

            "                float4 n40 = (float4)(n000, n001, n010, n011);",
            "                float4 n41 = (float4)(n100, n101, n110, n111);",

            "                float4 n4 = mix3d(n40, n41, smooth(fp.x));",
            "                float2 n2 = mix2d(n4.xy, n4.zw, smooth(fp.y));",
            "                float n = 0.5f - 0.5f * mix1d(n2.x, n2.y, smooth(fp.z));",
            "                return n;",
            "        }",

            "        float ridgedmultifractal3d(",
            "                float4 position,",
            "                float frequency,",
            "                float lacunarity,",
            "                float increment,",
            "                float octaves)",
            "        {",
            "                int i = 0;",
            "                float fi = 0.0f;",
            "                float remainder = 0.0f;",
            "                float sample = 0.0f;",
            "                float value = 0.0f;",
            "                int iterations = (int)octaves;",

            "                float threshold = 0.5f;",
            "                float offset = 1.0f;",
            "                float weight = 1.0f;",

            "                float signal = fabs( (1.0f - 2.0f * gradient_noise3d(position * frequency)) );",
            "                signal = offset - signal;",
            "                signal *= signal;",
            "                value = signal;",

            "                for ( i = 0; i < iterations; i++ )",
            "                {",
            "                        frequency *= lacunarity;",
            "                        weight = clamp( signal * threshold, 0.0f, 1.0f );",
            "                        signal = fabs( (1.0f - 2.0f * gradient_noise3d(position * frequency)) );",
            "                        signal = offset - signal;",
            "                        signal *= signal;",
            "                        signal *= weight;",
            "                        value += signal * pow( lacunarity, -fi * increment );",

            "                }",
            "                return value;",
            "        }",


            "        float4 cross3(float4 va, float4 vb)",
            "        {",
            "                float4 vc = (float4)(va.y*vb.z - va.z*vb.y,",
            "                                                                va.z*vb.x - va.x*vb.z,",
            "                                                                va.x*vb.y - va.y*vb.x, 0.0f);",
            "                return vc;",
            "        }",

            "        // mod: sg",
            "        /*float4 vload4_3(int index, float *va)",
            "        {",
            "                int i = 3*index;",
            "                float4 vc = (float4) (va[i], va[i+1], va[i+2], 1.0f);",
            "                return vc;",
            "        }",
            "        void vstore4_3(float4 vc, int index, float* va)",
            "        {",
            "                int i = 3*index;",
            "                va[i  ] = vc.x;",
            "                va[i+1] = vc.y;",
            "                va[i+2] = vc.z;",
            "        }*/",

            "        // initPos -> vertices  enqueueWrite (before first run)",
            "        // normals -> curNor    enqueueRead  (after each run)",
            "        // output  -> curPos    enqueueRead  (after each run)",
            "        //",

            "        __kernel void clDeform(",
            "                const __global float *vertices,",
            "                __global float *normals,",
            "                __global float *output,",
            "                float frequency,",
            "                float amplitude,",
            "                float phase,",
            "                float lacunarity,",
            "                float increment,",
            "                float octaves,",
            "                float roughness,",
            "                uint count)",
            "        {",
            "                int tx = get_global_id(0);",
            "                int ty = get_global_id(1);",
            "                int sx = get_global_size(0);",
            "                int index = ty * sx + tx;",
            "                if(index >= count)",
            "                        return;",

            "                int2 di = (int2)(tx, ty);",

            "                //float4 position = vload4_3((size_t)index, vertices);  // mod: sg",
            "                int ii = 3*index;",
            "                float4 position = (float4) (vertices[ii], vertices[ii+1], vertices[ii+2], 1.0f);",

            "                float4 normal = position;",
            "                position.w = 1.0f;",

            "                roughness /= amplitude;",
            "                float4 sample = position + (float4)(phase + 100.0f, phase + 100.0f, phase + 100.0f, 0.0f);",

            "                float4 dx = (float4)(roughness, 0.0f, 0.0f, 1.0f);",
            "                float4 dy = (float4)(0.0f, roughness, 0.0f, 1.0f);",
            "                float4 dz = (float4)(0.0f, 0.0f, roughness, 1.0f);",

            "                float f0 = ridgedmultifractal3d(sample, frequency, lacunarity, increment, octaves);",
            "                float f1 = ridgedmultifractal3d(sample + dx, frequency, lacunarity, increment, octaves);",
            "                float f2 = ridgedmultifractal3d(sample + dy, frequency, lacunarity, increment, octaves);",
            "                float f3 = ridgedmultifractal3d(sample + dz, frequency, lacunarity, increment, octaves);",

            "                float displacement = (f0 + f1 + f2 + f3) / 4.0;",

            "                float4 vertex = position + (amplitude * displacement * normal);",
            "                vertex.w = 1.0f;",

            "                normal.x -= (f1 - f0);",
            "                normal.y -= (f2 - f0);",
            "                normal.z -= (f3 - f0);",
            "                normal = normalized(normal / roughness);",

            "                //vstore4_3(vertex, (size_t)index, output);  // mod: sg",
            "                int jj = 3*index;",
            "                output[jj  ] = vertex.x;",
            "                output[jj+1] = ridgedmultifractal3d(sample, frequency, lacunarity, increment, octaves);",
            "                output[jj+2] = vertex.z;",

            "                //vstore4_3(normal, (size_t)index, normals); // mod: sg",
            "                normals[jj  ] = normal.x;",
            "                normals[jj+1] = normal.y;",
            "                normals[jj+2] = normal.z;",
            "        }"
        ]
    });

    Xflow.registerOperator("xflow.dummy", {
        outputs: [
            {type: 'float3', name: 'position'},
            {type: 'float3', name: 'normal'}
        ],
        params: [
            {type: 'float3', source: 'position' },
            {type: 'float3', source: 'normal'}

        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [

            "        // initPos -> vertices  enqueueWrite (before first run)",
            "        // normals -> curNor    enqueueRead  (after each run)",
            "        // output  -> curPos    enqueueRead  (after each run)",
            "        //",

            "        __kernel void clDeform(",
            "                const __global float *vertices,",
            "                __global float *normals,",
            "                __global float *output,",
            "                float frequency,",
            "                float amplitude,",
            "                float phase,",
            "                float lacunarity,",
            "                float increment,",
            "                float octaves,",
            "                float roughness,",
            "                uint count)",
            "        {",
            "                int tx = get_global_id(0);",
            "                int ty = get_global_id(1);",
            "                int sx = get_global_size(0);",
            "                int index = ty * sx + tx;",
            "                if(index >= count)",
            "                        return;",

            "                int2 di = (int2)(tx, ty);",

            "                //float4 position = vload4_3((size_t)index, vertices);  // mod: sg",
            "                int ii = 3*index;",
            "                float4 position = (float4) (vertices[ii], vertices[ii+1], vertices[ii+2], 1.0f);",

            "                int jj = 3*index;",
            "                normals[jj  ] = normal.x;",
            "                normals[jj+1] = normal.y;",
            "                normals[jj+2] = normal.z;",

            "                output[jj] = 2",
            "                output[jj+1] = 2",
            "                output[jj+2] = 2",
            "        }"
        ]
    });

    Xflow.registerOperator("xflow.gray", {
        outputs: [
            {type: 'texture', name: 'result', sizeof: 'image'}
        ],
        params: [
            {type: 'texture', source: 'image'}
        ],

        platform: Xflow.PLATFORM.CL,

        evaluate: [

            "uchar4 color = image[image_i];",
            "uchar lum = (uchar)(0.30f * color.x + 0.59f * color.y + 0.11f * color.z);",
            "result[image_i] = (uchar4)(lum, lum, lum, 255);"
        ]
    });





}());


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
    var webcl = XML3D.webcl,
        kernelManager = webcl.kernels,
        cmdQueue,
        simplex = new SimplexNoise();
    XML3D.debug.loglevel = 1;
    webcl.init("GPU");
    cmdQueue = webcl.createCommandQueue();

    (function () {

        kernelManager.register("clElevation",
            [
                "        __kernel void clElevation(",
                "                const __global float *positions,",
                "                const __global float *elevation,",
                "                const __global int *indices,",
                "                __global float *normals,",
                "                __global float *output,",
                "                uint count)",


                "        {",
                "                int tx = get_global_id(0)*3;",

                "                if(tx >= count*3)",
                "                        return;",
                "               int i = indices[tx]*3;",
                "               int ii = indices[tx+1]*3;",
                "               int iii = indices[tx+2]*3;",

                "                if(i >= count*3 || ii >= count*3 || iii >= count*3)",
                "                        return;",

                "                //Vertex v0;   ",
                "                output[i] = positions[i];",
                "                output[i+1] = -elevation[indices[tx]]/1000;",
                "                output[i+2] = positions[i+2];",

                "                //Vertex v1;   ",
                "                output[ii] = positions[ii];",
                "                output[ii+1] = -elevation[indices[tx+1]]/1000;",
                "                output[ii+2] = positions[ii+2];",

                "                //Vertex v2;   ",
                "                output[iii] = positions[iii];",
                "                output[iii+1] = -elevation[indices[tx+2]]/1000;",
                "                output[iii+2] = positions[iii+2];",

                "                float4 v0 = (float4) (output[i], output[i+1], output[i+2], 1.0f);",
                "                float4 v1 = (float4) (output[ii], output[ii+1], output[ii+2], 1.0f);",
                "                float4 v2 = (float4) (output[iii], output[iii+1], output[iii+2], 1.0f);",
                "                float4 normal = cross(v2-v0, v1-v0);",

                "                float l = 1.0/sqrt(normal.x*normal.x+normal.y*normal.y+normal.z*normal.z);",

                "                normals[i] = normal.x * l;",
                "                normals[i+1] = normal.y * l;",
                "                normals[i+2] = normal.z * l;",

                "                normals[ii] = normal.x * l;",
                "                normals[ii+1] = normal.y * l;",
                "                normals[ii+2] = normal.z * l;",

                "                normals[iii] = normal.x * l;",
                "                normals[iii+1] = normal.y * l;",
                "                normals[iii+2] = normal.z * l;",

                "        }"
            ].join("\n"));

        var kernel = webcl.kernels.getKernel("clElevation"),
            oldBufSize = 0,
            buffers = {initPosBuffer: null, elevationBuffer: null, indexBuffer: null, curPosBuffer: null, curNorBuffer: null};

        Xflow.registerOperator("xflow.clElevation", {
            outputs: [
                {type: 'float3', name: 'position', customAlloc: true},
                {type: 'float3', name: 'normal', customAlloc: true}
            ],
            params: [
                {type: 'float3', source: 'position' },
                {type: 'float3', source: 'normal'},
                {type: 'int', source: 'index'},
                {type: 'float3', source: 'elevation'}

            ],

            evaluate: function (newPos, newNor, position, normal, index, elevation) {
                //passing xflow operators input data

                //calculate vertices
                var nVertices = Math.floor((position.length)),
                    nIndices = Math.floor((index.length)),
                    nElevation = Math.floor((elevation.length)),

                // Setup buffers
                    bufSize = nVertices * Float32Array.BYTES_PER_ELEMENT, // size in bytes
                    bufSizeIndices = nIndices * Int32Array.BYTES_PER_ELEMENT, // size in bytes
                    bufSizeElevation = nElevation * Float32Array.BYTES_PER_ELEMENT, // size in bytes

                    initPosBuffer = buffers.initPosBuffer,
                    elevationBuffer = buffers.elevationBuffer,
                    indexBuffer = buffers.indexBuffer,
                    curPosBuffer = buffers.curPosBuffer,
                    curNorBuffer = buffers.curNorBuffer,

                    globalWorkSize = [],
                    localWorkSize = [];

                // InitCLBuffers
                if (bufSize !== oldBufSize) {
                    oldBufSize64 = bufSize;

                    if (initPosBuffer && elevationBuffer && indexBuffer && curNorBuffer && curPosBuffer) {
                        initPosBuffer.release();
                        elevationBuffer.release();
                        indexBuffer.release();
                        curNorBuffer.release();
                        curPosBuffer.release();
                    }

                    // Setup WebCL context using the default device of the first available platform
                    initPosBuffer = buffers.initPosBuffer = webcl.createBuffer(bufSize, "r");
                    elevationBuffer = buffers.elevationBuffer = webcl.createBuffer(bufSizeElevation, "r");
                    indexBuffer = buffers.indexBuffer = webcl.createBuffer(bufSizeIndices, "r");
                    curNorBuffer = buffers.curNorBuffer = webcl.createBuffer(bufSize, "rw");
                    curPosBuffer = buffers.curPosBuffer = webcl.createBuffer(bufSize, "rw");

                }

                try {
                    // Initial load of initial position data
                    cmdQueue.enqueueWriteBuffer(initPosBuffer, false, 0, bufSize, position, []);

                    //Write elevation data
                    cmdQueue.enqueueWriteBuffer(elevationBuffer, false, 0, bufSizeElevation, elevation, []);

                    //Write indices
                    cmdQueue.enqueueWriteBuffer(indexBuffer, false, 0, bufSizeIndices, index, []);

                    cmdQueue.finish();

                    kernelManager.setArgs(kernel, initPosBuffer, elevationBuffer, indexBuffer, curNorBuffer, curPosBuffer, new Int32Array([Math.floor(index.length / 3)]));

                    var localWS = [2];
                    var globalWS = [Math.ceil((index.length) / localWS[0]) * localWS[0]];

                    // Execute (enqueue) kernel
                    cmdQueue.enqueueNDRangeKernel(kernel, 1, [], globalWS, localWS, []);

                    // Read the result buffer from OpenCL device
                    cmdQueue.finish();

                    cmdQueue.enqueueReadBuffer(curPosBuffer, false, 0, bufSize, newPos, []);
                    cmdQueue.enqueueReadBuffer(curNorBuffer, false, 0, bufSize, newNor, []);

                    cmdQueue.finish();

                } catch (e) {
                    console.log(e.name, e.message);
                }

                return true;
            }
        });
    }());

    (function () {

        kernelManager.register("clDeform",
            ["        __constant int P_MASK = 255;",
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

            ].join("\n"));

        var kernel = webcl.kernels.getKernel("clDeform"),
            oldBufSize = 0,
            buffers = {initPosBuffer: null, curPosBuffer: null, curNorBuffer: null};

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
            evaluate: function (newPos, newNor, position, normal, amplitude, phase) {
                //passing xflow operators input data
                var NUM_VERTEX_COMPONENTS = 3,

                    initPos = position,

                // simulation parameters
                    frequency = 1.0,
                    amplitude = amplitude[0],
                    phase = phase[0],
                    lacunarity = 2.0,
                    increment = 1.5,
                    octaves = 0.5,
                    roughness = 0.025,

                //calculate vertices
                    nVertices = (position.length) / 3,

                // Setup buffers
                    bufSize = nVertices * NUM_VERTEX_COMPONENTS * Float32Array.BYTES_PER_ELEMENT, // size in bytes

                    initPosBuffer = buffers.initPosBuffer,
                    curPosBuffer = buffers.curPosBuffer,
                    curNorBuffer = buffers.curNorBuffer,

                    globalWorkSize = [],
                    localWorkSize = [];

                // InitCLBuffers
                if (bufSize !== oldBufSize) {
                    oldBufSize = bufSize;

                    if (initPosBuffer && curNorBuffer && curPosBuffer) {
                        initPosBuffer.release();
                        curNorBuffer.release();
                        curPosBuffer.release();
                    }

                    // Setup WebCL context using the default device of the first available platform
                    initPosBuffer = buffers.initPosBuffer = webcl.createBuffer(bufSize, "w");
                    curNorBuffer = buffers.curNorBuffer = webcl.createBuffer(bufSize, "rw");
                    curPosBuffer = buffers.curPosBuffer = webcl.createBuffer(bufSize, "rw");

                }

                // Get the maximum work group size for executing the kernel on the device
                //
                var workGroupSize = kernel.getWorkGroupInfo(webcl.getDevicesByType("GPU")[0], WebCL.CL_KERNEL_WORK_GROUP_SIZE);

                globalWorkSize[0] = 1;
                globalWorkSize[1] = 1;
                while (globalWorkSize[0] * globalWorkSize[1] < nVertices) {
                    globalWorkSize[0] = globalWorkSize[0] * 2;
                    globalWorkSize[1] = globalWorkSize[1] * 2;
                }

                localWorkSize[0] = globalWorkSize[0];
                localWorkSize[1] = globalWorkSize[1];
                while (localWorkSize[0] * localWorkSize[1] > workGroupSize) {
                    localWorkSize[0] = localWorkSize[0] / 2;
                    localWorkSize[1] = localWorkSize[1] / 2;
                }

                try {

                    // Initial load of initial position data
                    cmdQueue.enqueueWriteBuffer(initPosBuffer, true, 0, bufSize, initPos, []);

                    cmdQueue.finish();

                    kernelManager.setArgs(kernel, initPosBuffer, curNorBuffer, curPosBuffer,
                        new Float32Array([frequency]), new Float32Array([amplitude]),
                        new Float32Array([phase]), new Float32Array([lacunarity]),
                        new Float32Array([increment]), new Float32Array([octaves]),
                        new Float32Array([roughness]), new Float32Array([nVertices])
                    );

                    // Execute (enqueue) kernel
                    cmdQueue.enqueueNDRangeKernel(kernel, globalWorkSize.length, [], globalWorkSize, localWorkSize, []);

                    cmdQueue.finish();

                    // Read the result buffer from OpenCL device
                    cmdQueue.enqueueReadBuffer(curPosBuffer, true, 0, bufSize, newPos, []);
                    cmdQueue.enqueueReadBuffer(curNorBuffer, true, 0, bufSize, newNor, []);

                } catch (e) {
                    console.log(e.name, e.message)
                }

                return true;

            }
        });
    }());

    Xflow.registerOperator("xflow.customgrid", {
        outputs: [
            {type: 'float3', name: 'position', customAlloc: true},
            {type: 'float3', name: 'normal', customAlloc: true},
            {type: 'float2', name: 'texcoord', customAlloc: true},
            {type: 'int', name: 'index', customAlloc: true}
        ],
        params: [
            {type: 'int', source: 'area', array: true}
        ],
        alloc: function (areas, area) {
            var s = area[0];
            areas['position'] = s * s;
            areas['normal'] = s * s;
            areas['texcoord'] = s * s;
            areas['index'] = (s - 1) * (s - 1) * 6;
        },
        evaluate: function (position, normal, texcoord, index, area) {

            var s = area[0];

            // Create Positions
            for (var i = 0; i < position.length / 3; i++) {
                var offset = i * 3;
                position[offset] = (((i % s) / (s - 1)) - 0.5) * 2;
                position[offset + 1] = 0;
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

}());


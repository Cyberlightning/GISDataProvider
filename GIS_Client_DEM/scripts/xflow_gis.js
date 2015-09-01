
Xflow.registerOperator("xflow.vertexNormal", {
	outputs: [	{type: 'float3', name: 'normal', customAlloc: true} ],
	params:  [  {type: 'float3', source: 'position', array: true },
				{type: 'int', source: 'index' } ],
	alloc: function(sizes, position, index)
	{
		sizes['normal'] = (position.length/3);
	},
	evaluate: function(normal, position, index, info) {
		var vl = position.length;
		var il = index.length;
		
		for (var i=0; i < normal.length; i++)
			normal[i] = 0;


		// for each triangle j that shares ith vertex {
		for (var j=0; j < il; j+=3) {
			var A = 3*index[j  ];
			var B = 3*index[j+1];
			var C = 3*index[j+2];
			
			var Ax = position[A  ];
			var Ay = position[A+1];
			var Az = position[A+2];

			var Bx = position[B  ];
			var By = position[B+1];
			var Bz = position[B+2];

			var Cx = position[C  ];
			var Cy = position[C+1];
			var Cz = position[C+2];
			
			var Ux = Bx - Ax;
			var Uy = By - Ay;
			var Uz = Bz - Az;
			
			var Vx = Cx - Ax;
			var Vy = Cy - Ay;
			var Vz = Cz - Az;
			
			var S = [
				Uy*Vz - Uz*Vy,
				Uz*Vx - Ux*Vz,
				Ux*Vy - Uy*Vx
			];
			
			// normalize
			for (var k=0; k<3; k++) {
				var n = S[k];
				normal[A+k] += n;
				normal[B+k] += n;
				normal[C+k] += n;
			};
		}
		
		for (var i=0; i < normal.length; i+=3) {
			var l = Math.sqrt(normal[i]*normal[i]+normal[i+1]*normal[i+1]+normal[i+2]*normal[i+2]);
			normal[i  ] = normal[i  ] / l;
			normal[i+1] = normal[i+1] / l;
			normal[i+2] = normal[i+2] / l;
		}
	}
});



/**
 * Grid Generation
 */
Xflow.registerOperator("xflow.mygrid", {
    outputs: [	{type: 'float3', name: 'position', customAlloc: true},
				{type: 'float3', name: 'normal', customAlloc: true},
				{type: 'float2', name: 'texcoord', customAlloc: true},
				{type: 'int', name: 'index', customAlloc: true}],
    params:  [{type: 'int', source: 'size', array: true}],
    alloc: function(sizes, size)
    {
        var s = size[0];
        var t = (size.length > 1) ? size[1] : s;
        sizes['position'] = s* t;
        sizes['normal'] = s* t;
        sizes['texcoord'] = s* t;
        sizes['index'] = (s-1) * (t-1) * 6;
    },
    evaluate: function(position, normal, texcoord, index, size) {
		var s = size[0];
        var t = (size.length > 1) ? size[1] : s;
		var l = s*t;
		
        // Create Positions
		for(var i = 0; i < l; i++) {
			var offset = i*3;
			position[offset] =  (((i % s) / (s-1))-0.5)*2;
			position[offset+1] = 0;
			position[offset+2] = ((Math.floor(i/t) / (t-1))-0.5)*2;
		}

        // Create Normals
		for(var i = 0; i < l; i++) {
			var offset = i*3;
			normal[offset] =  0;
			normal[offset+1] = 1;
			normal[offset+2] = 0;
		}
        // Create Texture Coordinates
		for(var i = 0; i < l; i++) {
			var offset = i*2;
            texcoord[offset] = (i%s) / (s-1);
            texcoord[offset+1] = 1.0 - (Math.floor(i/t) / (t-1));
		}

        // Create Indices for triangles
		var tl = (s-1) * (t-1);
		var offset = 0;
		for(var i = 0; i < tl; i++) {
			var base = i + Math.floor(i / (s-1));
			index[offset++] = base + 1;
			index[offset++] = base;
			index[offset++] = base + s;
			index[offset++] = base + s;
			index[offset++] = base + s + 1;
			index[offset++] = base + 1;
		}
	}
});



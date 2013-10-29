/**
 * @author sole / http://soledadpenades.com
 * @author mr.doob / http://mrdoob.com
 * @author Robert Eisele / http://www.xarg.org
 * @author Philippe / http://philippe.elsass.me
 * @author Robert Penner / http://www.robertpenner.com/easing_terms_of_use.html
 * @author Paul Lewis / http://www.aerotwist.com/
 * @author lechecacharro
 * @author Josh Faul / http://jocafa.com/
 * @author egraether / http://egraether.com/
 */

var TWEEN = TWEEN || ( function () {

	var _tweens = [];

	return {

		REVISION: '6',

		getAll: function () {

			return _tweens;

		},

		removeAll: function () {

			_tweens = [];

		},

		add: function ( tween ) {

			_tweens.push( tween );

		},

		remove: function ( tween ) {

			var i = _tweens.indexOf( tween );

			if ( i !== -1 ) {

				_tweens.splice( i, 1 );

			}

		},

		update: function ( time ) {

			var i = 0;
			var num_tweens = _tweens.length;
			var time = time !== undefined ? time : Date.now();

			while ( i < num_tweens ) {

				if ( _tweens[ i ].update( time ) ) {

					i ++;

				} else {

					_tweens.splice( i, 1 );
					num_tweens --;

				}

			}

		}

	};

} )();

TWEEN.Tween = function ( object ) {

	var _object = object;
	var _valuesStart = {};
	var _valuesEnd = {};
	var _duration = 1000;
	var _delayTime = 0;
	var _startTime = null;
	var _easingFunction = TWEEN.Easing.Linear.None;
	var _interpolationFunction = TWEEN.Interpolation.Linear;
	var _chainedTween = null;
	var _onUpdateCallback = null;
	var _onCompleteCallback = null;

	this.to = function ( properties, duration ) {

		if ( duration !== null ) {

			_duration = duration;

		}

		_valuesEnd = properties;

		return this;

	};

	this.start = function ( time ) {

		TWEEN.add( this );

		_startTime = time !== undefined ? time : Date.now();
		_startTime += _delayTime;

		for ( var property in _valuesEnd ) {

			// This prevents the engine from interpolating null values
			if ( _object[ property ] === null ) {

				continue;

			}

			// check if an Array was provided as property value
			if ( _valuesEnd[ property ] instanceof Array ) {

				if ( _valuesEnd[ property ].length === 0 ) {

					continue;

				}

				// create a local copy of the Array with the start value at the front
				_valuesEnd[ property ] = [ _object[ property ] ].concat( _valuesEnd[ property ] );

			}

			_valuesStart[ property ] = _object[ property ];

		}

		return this;

	};

	this.stop = function () {

		TWEEN.remove( this );
		return this;

	};

	this.delay = function ( amount ) {

		_delayTime = amount;
		return this;

	};

	this.easing = function ( easing ) {

		_easingFunction = easing;
		return this;

	};

	this.interpolation = function ( interpolation ) {

		_interpolationFunction = interpolation;
		return this;

	};

	this.chain = function ( chainedTween ) {

		_chainedTween = chainedTween;
		return this;

	};

	this.onUpdate = function ( onUpdateCallback ) {

		_onUpdateCallback = onUpdateCallback;
		return this;

	};

	this.onComplete = function ( onCompleteCallback ) {

		_onCompleteCallback = onCompleteCallback;
		return this;

	};

	this.update = function ( time ) {

		if ( time < _startTime ) {

			return true;

		}

		var elapsed = ( time - _startTime ) / _duration;
		elapsed = elapsed > 1 ? 1 : elapsed;

		var value = _easingFunction( elapsed );

		for ( var property in _valuesStart ) {

			var start = _valuesStart[ property ];
			var end = _valuesEnd[ property ];

			if ( end instanceof Array ) {

				_object[ property ] = _interpolationFunction( end, value );

			} else {

				_object[ property ] = start + ( end - start ) * value;

			}

		}

		if ( _onUpdateCallback !== null ) {

			_onUpdateCallback.call( _object, value );

		}

		if ( elapsed == 1 ) {

			if ( _onCompleteCallback !== null ) {

				_onCompleteCallback.call( _object );

			}

			if ( _chainedTween !== null ) {

				_chainedTween.start();

			}

			return false;

		}

		return true;

	};

};

TWEEN.Easing = {

	Linear: {

		None: function ( k ) {

			return k;

		}

	},

	Quadratic: {

		In: function ( k ) {

			return k * k;

		},

		Out: function ( k ) {

			return k * ( 2 - k );

		},

		InOut: function ( k ) {

			if ( ( k *= 2 ) < 1 ) return 0.5 * k * k;
			return - 0.5 * ( --k * ( k - 2 ) - 1 );

		}

	},

	Cubic: {

		In: function ( k ) {

			return k * k * k;

		},

		Out: function ( k ) {

			return --k * k * k + 1;

		},

		InOut: function ( k ) {

			if ( ( k *= 2 ) < 1 ) return 0.5 * k * k * k;
			return 0.5 * ( ( k -= 2 ) * k * k + 2 );

		}

	},

	Quartic: {

		In: function ( k ) {

			return k * k * k * k;

		},

		Out: function ( k ) {

			return 1 - --k * k * k * k;

		},

		InOut: function ( k ) {

			if ( ( k *= 2 ) < 1) return 0.5 * k * k * k * k;
			return - 0.5 * ( ( k -= 2 ) * k * k * k - 2 );

		}

	},

	Quintic: {

		In: function ( k ) {

			return k * k * k * k * k;

		},

		Out: function ( k ) {

			return --k * k * k * k * k + 1;

		},

		InOut: function ( k ) {

			if ( ( k *= 2 ) < 1 ) return 0.5 * k * k * k * k * k;
			return 0.5 * ( ( k -= 2 ) * k * k * k * k + 2 );

		}

	},

	Sinusoidal: {

		In: function ( k ) {

			return 1 - Math.cos( k * Math.PI / 2 );

		},

		Out: function ( k ) {

			return Math.sin( k * Math.PI / 2 );

		},

		InOut: function ( k ) {

			return 0.5 * ( 1 - Math.cos( Math.PI * k ) );

		}

	},

	Exponential: {

		In: function ( k ) {

			return k === 0 ? 0 : Math.pow( 1024, k - 1 );

		},

		Out: function ( k ) {

			return k === 1 ? 1 : 1 - Math.pow( 2, - 10 * k );

		},

		InOut: function ( k ) {

			if ( k === 0 ) return 0;
			if ( k === 1 ) return 1;
			if ( ( k *= 2 ) < 1 ) return 0.5 * Math.pow( 1024, k - 1 );
			return 0.5 * ( - Math.pow( 2, - 10 * ( k - 1 ) ) + 2 );

		}

	},

	Circular: {

		In: function ( k ) {

			return 1 - Math.sqrt( 1 - k * k );

		},

		Out: function ( k ) {

			return Math.sqrt( 1 - --k * k );

		},

		InOut: function ( k ) {

			if ( ( k *= 2 ) < 1) return - 0.5 * ( Math.sqrt( 1 - k * k) - 1);
			return 0.5 * ( Math.sqrt( 1 - ( k -= 2) * k) + 1);

		}

	},

	Elastic: {

		In: function ( k ) {

			var s, a = 0.1, p = 0.4;
			if ( k === 0 ) return 0;
			if ( k === 1 ) return 1;
			if ( !a || a < 1 ) { a = 1; s = p / 4; }
			else s = p * Math.asin( 1 / a ) / ( 2 * Math.PI );
			return - ( a * Math.pow( 2, 10 * ( k -= 1 ) ) * Math.sin( ( k - s ) * ( 2 * Math.PI ) / p ) );

		},

		Out: function ( k ) {

			var s, a = 0.1, p = 0.4;
			if ( k === 0 ) return 0;
			if ( k === 1 ) return 1;
			if ( !a || a < 1 ) { a = 1; s = p / 4; }
			else s = p * Math.asin( 1 / a ) / ( 2 * Math.PI );
			return ( a * Math.pow( 2, - 10 * k) * Math.sin( ( k - s ) * ( 2 * Math.PI ) / p ) + 1 );

		},

		InOut: function ( k ) {

			var s, a = 0.1, p = 0.4;
			if ( k === 0 ) return 0;
			if ( k === 1 ) return 1;
			if ( !a || a < 1 ) { a = 1; s = p / 4; }
			else s = p * Math.asin( 1 / a ) / ( 2 * Math.PI );
			if ( ( k *= 2 ) < 1 ) return - 0.5 * ( a * Math.pow( 2, 10 * ( k -= 1 ) ) * Math.sin( ( k - s ) * ( 2 * Math.PI ) / p ) );
			return a * Math.pow( 2, -10 * ( k -= 1 ) ) * Math.sin( ( k - s ) * ( 2 * Math.PI ) / p ) * 0.5 + 1;

		}

	},

	Back: {

		In: function ( k ) {

			var s = 1.70158;
			return k * k * ( ( s + 1 ) * k - s );

		},

		Out: function ( k ) {

			var s = 1.70158;
			return --k * k * ( ( s + 1 ) * k + s ) + 1;

		},

		InOut: function ( k ) {

			var s = 1.70158 * 1.525;
			if ( ( k *= 2 ) < 1 ) return 0.5 * ( k * k * ( ( s + 1 ) * k - s ) );
			return 0.5 * ( ( k -= 2 ) * k * ( ( s + 1 ) * k + s ) + 2 );

		}

	},

	Bounce: {

		In: function ( k ) {

			return 1 - TWEEN.Easing.Bounce.Out( 1 - k );

		},

		Out: function ( k ) {

			if ( k < ( 1 / 2.75 ) ) {

				return 7.5625 * k * k;

			} else if ( k < ( 2 / 2.75 ) ) {

				return 7.5625 * ( k -= ( 1.5 / 2.75 ) ) * k + 0.75;

			} else if ( k < ( 2.5 / 2.75 ) ) {

				return 7.5625 * ( k -= ( 2.25 / 2.75 ) ) * k + 0.9375;

			} else {

				return 7.5625 * ( k -= ( 2.625 / 2.75 ) ) * k + 0.984375;

			}

		},

		InOut: function ( k ) {

			if ( k < 0.5 ) return TWEEN.Easing.Bounce.In( k * 2 ) * 0.5;
			return TWEEN.Easing.Bounce.Out( k * 2 - 1 ) * 0.5 + 0.5;

		}

	}

};

TWEEN.Interpolation = {

	Linear: function ( v, k ) {

		var m = v.length - 1, f = m * k, i = Math.floor( f ), fn = TWEEN.Interpolation.Utils.Linear;

		if ( k < 0 ) return fn( v[ 0 ], v[ 1 ], f );
		if ( k > 1 ) return fn( v[ m ], v[ m - 1 ], m - f );

		return fn( v[ i ], v[ i + 1 > m ? m : i + 1 ], f - i );

	},

	Bezier: function ( v, k ) {

		var b = 0, n = v.length - 1, pw = Math.pow, bn = TWEEN.Interpolation.Utils.Bernstein, i;

		for ( i = 0; i <= n; i++ ) {
			b += pw( 1 - k, n - i ) * pw( k, i ) * v[ i ] * bn( n, i );
		}

		return b;

	},

	CatmullRom: function ( v, k ) {

		var m = v.length - 1, f = m * k, i = Math.floor( f ), fn = TWEEN.Interpolation.Utils.CatmullRom;

		if ( v[ 0 ] === v[ m ] ) {

			if ( k < 0 ) i = Math.floor( f = m * ( 1 + k ) );

			return fn( v[ ( i - 1 + m ) % m ], v[ i ], v[ ( i + 1 ) % m ], v[ ( i + 2 ) % m ], f - i );

		} else {

			if ( k < 0 ) return v[ 0 ] - ( fn( v[ 0 ], v[ 0 ], v[ 1 ], v[ 1 ], -f ) - v[ 0 ] );
			if ( k > 1 ) return v[ m ] - ( fn( v[ m ], v[ m ], v[ m - 1 ], v[ m - 1 ], f - m ) - v[ m ] );

			return fn( v[ i ? i - 1 : 0 ], v[ i ], v[ m < i + 1 ? m : i + 1 ], v[ m < i + 2 ? m : i + 2 ], f - i );

		}

	},

	Utils: {

		Linear: function ( p0, p1, t ) {

			return ( p1 - p0 ) * t + p0;

		},

		Bernstein: function ( n , i ) {

			var fc = TWEEN.Interpolation.Utils.Factorial;
			return fc( n ) / fc( i ) / fc( n - i );

		},

		Factorial: ( function () {

			var a = [ 1 ];

			return function ( n ) {

				var s = 1, i;
				if ( a[ n ] ) return a[ n ];
				for ( i = n; i > 1; i-- ) s *= i;
				return a[ n ] = s;

			}

		} )(),

		CatmullRom: function ( p0, p1, p2, p3, t ) {

			var v0 = ( p2 - p0 ) * 0.5, v1 = ( p3 - p1 ) * 0.5, t2 = t * t, t3 = t * t2;
			return ( 2 * p1 - 2 * p2 + v0 + v1 ) * t3 + ( - 3 * p1 + 3 * p2 - 2 * v0 - v1 ) * t2 + v0 * t + p1;

		}

	}

};
/** @namespace **/
var XMOT = XMOT || {};

/** @define {string} */
XMOT.version = 'DEVELOPMENT SNAPSHOT (June 19 2012)';

(function() {

    /**
     * A MotionFactory.
     * @interface
     */
    var MotionFactory = function() {};
    var m = MotionFactory.prototype;

    /**
     * Creates a Moveable out of the given object
     * @param {Object} object base for the Moveable
     * @param {Constraint} constraint Constrain movement
     * @return {Moveable} created Moveable
     */
    m.createMoveable = function(object, constraint){};

    /**
     * Creates an Animatable out of the given object
     * @param {Object} object base for the Animatable
     * @param {Constraint} constraint Constrain movement
     * @return {Animatable} created Animatable
     */
    m.createAnimatable = function(object, constraint){};

    /**
     * Creates a KeyframeAnimation
     * @param {string} name name
     * @param {string} type "Position" or "Orientation"
     * @param {Object} element KeyframeAnimation, keyframes and corresponding positions or orientations
     * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}=} opt options
     * @return {KeyframeAnimation} created KeyFrameAnimation
     */
    m.createKeyframeAnimation = function(name, type, element, opt){};

    /**
     * Creates a ParameterAnimation
     * @param {string} name name
     * @param {Object} element ParameterAnimation, keys and corresponding parameters
     * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}=} opt options
     * @return {ParamterAnimation} created ParameterAnimation
     */
    m.createParameterAnimation = function(name, element, opt){};


    /**
     * A Moveable.
     * @interface
     */
    var Moveable = function() {};
    var p = Moveable.prototype;

    /**
     * Sets the absolute position of the Moveable in local space.
     * @param {Array.<number>} position position as 3d vector in local space
     * @return {Moveable} the Moveable
     */
    p.setPosition = function(position){};

    /**
     * Sets the absolute orientation of the Movebale in local space.
     * @param {Array.<number>} orientation orientation as quaternion in local space
     * @return {Moveable} the Moveable
     */
    p.setOrientation = function(orientation){};

    /**
     * Gets the current position
     * @return {Array.<number>} position
     */
    p.getPosition = function(){};

    /**
     * Gets the current orientation as quaternion
     * @return {Array.<number>} orientation
     */
    p.getOrientation = function(){};

    /**
     * Translate the Moveable by a given Vector.
     * @param {Array.<number>} translation 3d Vector
     * @return {Moveable} the Moveable
     */
    p.translate = function(translation){};

    /**
     * Rotates the Moveable by a given Quaternion.
     * @param {Array.<number>} rotation Quaternion
     * @return {Moveable} the Moveable
     */
    p.rotate = function(rotation){};

    /**
     * Interpolated translation over time to position in local space.
     * The animation is put into a fifo-queue and will be eventually executed.
     * @param {Array.<number>|undefined} position local space Vector
     * @param {Array.<number>|undefined} orientation orientation Quaternion
     * @param {number} time when to reach the position, in milliseconds
     * @param {{delay: number, easing: function, queueing: Boolean, callback: function}=} opt options
     * @return {Moveable} the Moveable
     */
    p.moveTo = function(position, orientation, time, opt){};

    /**
     * Returns true if a movement is currently in progress
     * @return {Boolean}
     */
    p.movementInProgress = function(){};

    /**
     * Stops the current movement and cancels every queued movement.
     * @return {Moveable} the Moveable
     */
    p.stop = function(){};

    /**
     * Sets a constraint for the Moveable. The constraint is checked
     * @param {Constraint} constraint Set a constraint to the Moveable
     */
    p.setContraint = function(constraint){};



    /**
     * An Animatable
     * @extends Moveable
     * @interface
     */
    var Animatable = function(){};
    var a = Animatable.prototype;

    /**
     * Add an Animation to the Animatable
     * @param {Animation} animation Animation
     * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}=} opt options
     * @return {Animatable} the Animatable
     */
    a.addAnimation = function(animation, opt){};

    /**
     * Starts an animation
     * @param {string} name animation, that will be started
     * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}=} opt options
     * @return {number} id id of the animation
     */
    a.startAnimation = function(name, opt){};

    /**
     * Stops an animation
     * @param {string} id Animation ID
     * @return {Animatable} the Animatable
     */
    a.stopAnimation = function(id){};



    /**
     * An Animation
     * @interface
     */
    var Animation = function(){};
    var k = Animation.prototype;

    /**
     * Sets the state of the animatable at time x of the animation
     * @param {Animatable} animatable
     * @param {number} currentTime
     * @param {number} startTime
     * @param {number} endTime
     * @param {function=} easing
     */
    k.applyAnimation = function(animatable, currentTime, startTime, endTime, easing){};

	/**
	 * Set Options
	 * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}} opt options
	 */
	k.setOptions = function(opt){};

	/**
	 * Gets the value of an option, the option can be requested by its name
	 * @param {string} name of the option
	 * @retrurn {object} the requested option value
	 */
	k.getOption = function(name){};


	/**
	 * A CombinedAnimation
	 * @extends Animation
	 * @interface
	 */
	var CombinedAnimation = function(){};
	var ca = CombinedAnimation.prototype;

	/**
	 * Add an animation to the collection
	 * @param {Animation} animation
	 * @param {{duration: number, loop: number, delay: number, easing: function, callback: function}=} opt options
	 */
	ca.addAnimation = function(animation, opt){};



	/**
     * A Constraint
     * @interface
     */
    var Constraint = function(){};
    var c = Constraint.prototype;

    /**
     * Checks if a rotation operation is valid.
     * @param {Array.<number>} rotation Quaternion
     * @param {Moveable} moveable Moveable
     * @return {boolean} returns true if the operation is valid, false otherwise
     */
    c.constrainRotation = function(rotation, moveable){};

    /**
     * Checks if a translation operation is valid.
     * @param {Array.<number>} translation 3d Vector
     * @param {Moveable} moveable Moveable
     * @return {boolean} returns true if the operation is valid, false otherwise
     */
    c.constrainTranslation = function(translation, moveable){};
}());//namespace for the goog closure stuff
var goog = goog || {};

(function() {

// the following two functions are copied from closure: goog.base since the complete goog.base did not work with firefox
// TODO: implement the closure build tools (python script) in the ant build process and let the tools get the minimal set
// of used functions instead of copying and hacking those two.

/**
 * Inherit the prototype methods from one constructor into another.
 *
 * Usage:
 * <pre>
 * function ParentClass(a, b) { }
 * ParentClass.prototype.foo = function(a) { }
 *
 * function ChildClass(a, b, c) {
 *   goog.base(this, a, b);
 * }
 * goog.inherits(ChildClass, ParentClass);
 *
 * var child = new ChildClass('a', 'b', 'see');
 * child.foo(); // works
 * </pre>
 *
 * In addition, a superclass' implementation of a method can be invoked
 * as follows:
 *
 * <pre>
 * ChildClass.prototype.foo = function(a) {
 *   ChildClass.superClass_.foo.call(this, a);
 *   // other code
 * };
 * </pre>
 *
 * @param {Function} childCtor Child class.
 * @param {Function} parentCtor Parent class.
 */
goog.inherits = function(childCtor, parentCtor) {
  /** @constructor */
  function tempCtor() {};
  tempCtor.prototype = parentCtor.prototype;
  childCtor.superClass_ = parentCtor.prototype;
  childCtor.prototype = new tempCtor();
  childCtor.prototype.constructor = childCtor;
};

/**
 * Call up to the superclass.
 *
 * If this is called from a constructor, then this calls the superclass
 * contructor with arguments 1-N.
 *
 * If this is called from a prototype method, then you must pass
 * the name of the method as the second argument to this function. If
 * you do not, you will get a runtime error. This calls the superclass'
 * method with arguments 2-N.
 *
 * This function only works if you use goog.inherits to express
 * inheritance relationships between your classes.
 *
 * This function is a compiler primitive. At compile-time, the
 * compiler will do macro expansion to remove a lot of
 * the extra overhead that this function introduces. The compiler
 * will also enforce a lot of the assumptions that this function
 * makes, and treat it as a compiler error if you break them.
 *
 * @param {!Object} me Should always be "this".
 * @param {*=} opt_methodName The method name if calling a super method.
 * @param {...*} var_args The rest of the arguments.
 * @return {*} The return value of the superclass method.
 */
goog.base = function(me, opt_methodName, var_args) {
  var caller = arguments.callee.caller;
  if (caller.superClass_) {
    // This is a constructor. Call the superclass constructor.
    return caller.superClass_.constructor.apply(
        me, Array.prototype.slice.call(arguments, 1));
  }

  var args = Array.prototype.slice.call(arguments, 2);
  var foundCaller = false;
  for (var ctor = me.constructor;
       ctor; ctor = ctor.superClass_ && ctor.superClass_.constructor) {
    if (ctor.prototype[opt_methodName] === caller) {
      foundCaller = true;
    } else if (foundCaller) {
      return ctor.prototype[opt_methodName].apply(me, args);
    }
  }

  // If we did not find the caller in the prototype chain,
  // then one of two things happened:
  // 1) The caller is an instance method.
  // 2) This method was not called by the right caller.
  if (me[opt_methodName] === caller) {
    return me.constructor.prototype[opt_methodName].apply(me, args);
  } else {
    throw Error(
        'goog.base called from a method of one name ' +
        'to a method of a different name');
  }
};

// ----------------------------------------------------------------------------

/**
 * global variable, used to check if an animation or movement is currently in progress
 */
var animating = false;

/**
 * global variable, set a function, which is called within the animation loop
 */
var animationHook = undefined;

/**
 * Updates all the Tweens until all animations are finished and calls the hook.
 */
function animate(){
	if(TWEEN.getAll().length || XMOT.animationHook) {
		window.requestAnimFrame(XMOT.animate);
		if(XMOT.animationHook != undefined) XMOT.animationHook();
		TWEEN.update();
	}
	else
		XMOT.animating = false;
};

/**
 * Converts axis angle representation into an quaternion
 * @param {Array.<number>} axis
 * @param {number} angle
 * @return {Array.<number>} quaternion
 */
function axisAngleToQuaternion(axis, angle){
	var normAxis = XMOT.normalizeVector(axis);
	var quat = [];
	var s = Math.sin(angle/2);
	quat[0] = normAxis[0] *s;
	quat[1] = normAxis[1] *s;
	quat[2] = normAxis[2] *s;
	quat[3] = Math.cos(angle/2);
	return quat;
};

/**
 * Normalizes a 3D vector
 * @param {Array.<number>} vector
 * @return {Array.<number>} normalized vector
 */
function normalizeVector(vector){
	var length = Math.sqrt( vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2] );
	if(length == 0) return vector;
	return [vector[0]/length, vector[1]/length, vector[2]/length];
};

/**
 * Converts a quaternion into an axis angle representation
 * @param{Array.<number>} quaternion
 * @param{{axis:Array.<number>}, angle:number} quaternion
 */
function quaternionToAxisAngle(quat){
	quat4.normalize(quat); //normalise to avoid erros that may happen if qw > 1
	var angle = 2*Math.acos(quat[3]);
	var s = Math.sqrt(1-quat[3]*quat[3]);
	if(s < 0.00001 ) s = 1; //avoid div by zero, direction not important for small s
	var x = quat[0]/s;
	var y = quat[1]/s;
	var z = quat[2]/s;
	return {axis:[x,y,z], angle:angle};
};

//export
XMOT.animate = animate;
XMOT.animating = animating;
XMOT.animationHook = animationHook;
XMOT.axisAngleToQuaternion = axisAngleToQuaternion;
XMOT.normalizeVector = normalizeVector;
XMOT.quaternionToAxisAngle = quaternionToAxisAngle;
}());(function() {

    /**
     * A Moveable implementation.
     * @constructor
     * @implements{Moveable}
     */
    function ClientMoveable(object, transform, constraint) {
    	/**
		 * Object which shall be moveable
		 * @protected
		 * @type {Object}
		 */
		this.object = object;
		/**
		 * Transform coords of the object and the Moveable
		 * @protected
		 * @type {}
		 */
		this.transform = transform;
		/**
		 * Constraint of the movement
		 * @protected
		 * @type {Constraint}
		 */
		this.constraint = constraint;
		/**
		 * Queue of movements
		 * @private
		 * @type {Array.<{tween: tween, startPosition:Array.<number>, endPosition:Array.<number>, startOrientation:Array.<number>, endOrientation:Array.<number>}>}
		 */
		this.motionQueue = new Array();
    };

    var p = ClientMoveable.prototype;

    /** @inheritDoc */
    p.setPosition = function(position){
		//make the setPosition a translation in order to work with the constraint
    	//TODO: make this somehow different?
		return this.translate([position[0]-this.transform.translation.x, position[1]-this.transform.translation.y, position[2]-this.transform.translation.z]);
    };

    /** @inheritDoc */
	p.setOrientation = function(orientation){
		if(this.constraint.constrainRotation(orientation, this)){
			this.transform.rotation.setQuaternion( new XML3DVec3(orientation[0],orientation[1],orientation[2]), orientation[3] );
		}
		return this;
    };

    /** @inheritDoc */
    p.getPosition = function(){
    	return [this.transform.translation.x, this.transform.translation.y, this.transform.translation.z];
    };

    /** @inheritDoc */
    p.getOrientation = function(){
    	var axis = this.transform.rotation.axis;
    	var angle = this.transform.rotation.angle;
    	return XMOT.axisAngleToQuaternion([axis.x, axis.y, axis.z], angle);
    };

    /** @inheritDoc */
    p.translate = function(translation){
		if(this.constraint.constrainTranslation(translation, this))
			this.transform.translation.set(this.transform.translation.add( new XML3DVec3(translation[0],translation[1],translation[2]) ));
		return this;
    };

    /** @inheritDoc */
    p.rotate = function(orientation){
		var modifier = new XML3DRotation();
		modifier.setQuaternion( new XML3DVec3(orientation[0],orientation[1],orientation[2]), orientation[3] );
		var destination = this.transform.rotation.multiply( modifier );
		if(this.constraint.constrainRotation(orientation, this))
			this.transform.rotation.set(destination);
		return this;
    };

    /** @inheritDoc */
    p.moveTo = function(position, orientation, time, opt){
    	//no movement needed
    	var queueingAllowed = (opt && opt.queueing != undefined) ? opt.queueing : true; 
		if( (position == undefined && orientation == undefined) || //nowhere to moveto
				( !queueingAllowed && this.motionQueue.length) ) //queuing forbiden, but something in progress
					return this;

		//crate new queue entry of the new given data:
		var newEntry = {};
		var tween = new TWEEN.Tween({t:0}).to({t:time}, time);
		if(opt && opt.delay != undefined) tween.delay(opt.delay);
		var that = this;
		var easing = undefined;
		if(opt && opt.easing != undefined) easing = opt.easing;
		//update callback
		tween.onUpdate( function() {
			//this is the data interpolated by the tween
			that.movement(this.t, 0, time, easing);
		} );
		//callback on complete
		tween.onComplete( function(){
			//this is the data interpolated by the tween
			//remove finished tween from the beginning of the queue
			that.motionQueue.shift();
			//start next tween (beginning of the queue), if there is any in the queue
			if(that.motionQueue.length != 0){
				that.motionQueue[0].tween.start();
			}
			//callback after the movement finished
			if(opt && opt.callback && typeof(opt.callback) === "function")
				opt.callback();
		});
		newEntry.tween = tween;
		newEntry.endPosition = position;
		newEntry.endOrientation = orientation;
		//default start values, are the current values
		newEntry.startPosition = this.getPosition();
		newEntry.startOrientation = this.getOrientation();
		if(this.motionQueue.length != 0){
			//we are not the first, we start, where the motion before ended
			//seek the last defined end values
			var q = this.motionQueue;
			var length = q.length;
			var i = 0;
			var tmp = undefined;
			for(i = length-1; i>-1; i--){
				tmp = q[i].endPosition;
				if(tmp != undefined){
					newEntry.startPosition = tmp;
					break;
				}
			}
			//2nd loop instead of more complex if statements
			for(i = length-1; i>-1; i--){
				if(q[i].endOrientation != undefined){
					newEntry.startOrientation = q[i].endOrientation;
					break;
				}
			}
		}

		//push tween to the end of the queue and start if queue was empty
		this.motionQueue.push(newEntry);
		if( this.motionQueue.length-1 == 0){
			newEntry.tween.start();
			if(!XMOT.animating) {
				XMOT.animate();
				XMOT.animating = true;
			}
		}
		return this;
    };

    /**
     * Applies one movement step to the moveable
     * @private
     */
    p.movement = function(currentTime, startTime, endTime, easing){
		var t = (currentTime - startTime) / (endTime - startTime);
		if(easing && typeof(easing) === "function") t = easing(t); //otherwise its linear
		var pos = this.interpolatePosition(t);
		var ori = this.interpolateOrientation(t);
		this.setValue(pos, ori);
    };

    /**
     * Interpolates the position of the current movement
     * @private
     * @param t interpolation parameter
     */
    p.interpolatePosition = function(t){
		var end = this.motionQueue[0].endPosition;
		if(end == undefined) return undefined;
		var start = this.motionQueue[0].startPosition;
		var ret = [];
		var i = 0;
		for(i=0; i<start.length; i++ ){
			ret[i] = start[i] + ( end[i] - start[i] ) * t;
		}
		return ret;
    };

    /**
     * interpoaltes the orientation of the current movement
     * @private
     * @param t interpolation paramater
     */
    p.interpolateOrientation = function(t){
		var end = this.motionQueue[0].endOrientation;
		if(end == undefined) return undefined;
		var start = this.motionQueue[0].startOrientation;
		//the newely created quat gets filled with the result and returned
		return quat4.slerp(start, end, t, quat4.create());
    };

    /**
	 * Set position and animation of the moveable
	 * @private
	 * @param {Array.<number>|undefined}
	 * @param {Array.<number>|undefined}
	 */
	p.setValue = function(position, orientation){
		if(position != undefined)
			this.setPosition(position);
		if(orientation != undefined)
			this.setOrientation(orientation);
	};

	/** @inheritDoc */
	p.movementInProgress = function(){
		return this.motionQueue.length > 0;
	};

    /**@inheritDoc */
    p.stop = function(){
		this.motionQueue.shift().tween.stop();
		this.motionQueue = []; //clear array
    };

    /** @inheritDoc */
    p.setConstraint = function(constraint){
		this.constraint = constraint;
    };

    //export
    XMOT.ClientMoveable = ClientMoveable;

}());
(function(){
	/**
	 * ClientMotionFactory implementation
	 * @constructor
	 * @implements{MotionFactory}
	 */
	function ClientMotionFactory(){
		//TODO: sync with server - sirikata
	};

	var m = ClientMotionFactory.prototype;

	/** @inheritDoc */
	m.createMoveable = function(obj, constraint){
		var t = XML3D.URIResolver.resolve(obj.getAttribute("transform"), obj.ownerDocument);
		if (!t) {
			throw "Object does not have a transfrom property.";
			return null;
		}
		return new XMOT.ClientMoveable(obj, t, constraint);
	};

	/** @inheritDoc */
	m.createAnimatable = function(obj, constraint){
		var t = XML3D.URIResolver.resolve(obj.getAttribute("transform"), obj.ownerDocument);
		if (!t) {
			throw "Object does not have a transfrom property.";
			return null;
		}
		return new XMOT.ClientAnimatable(obj, t, constraint);
	};

	/** @inheritDoc */
    m.createKeyframeAnimation = function(name, type, element, opt){
		//TODO: this works with WebGL only?
		//TODO: error handling?

		var child = element.firstElementChild;
		var keys = child.value;
		child = child.nextElementSibling;
		var values = child.value;
		if(!keys || !values){
			throw "Object is not a valid keyframe animation";
			return null;
		}

		// opt is just passed to the constructor and not further handled here
		if(type === "Position")
			return new XMOT.ClientKeyframeAnimation(name, keys, values, undefined, opt);
		else if(type === "Orientation")
			return new XMOT.ClientKeyframeAnimation(name, keys, undefined, values, opt);
		else if(type === "Both"){
			child = child.nextElementSibling;
			var secondValues = child.value;
			if(!secondValues){
				throw "Specified both animations types but did not provide two series of values";
				return null;
			}
			if(values.length*3 == keys.length) //first values are position
				return new XMOT.ClientKeyframeAnimation(name, keys, values, secondValues, opt);
			else //secondValues are position
				return new XMOT.ClientKeyframeAnimation(name, keys, secondValues, values, opt);
		}else{
			throw "Type must be either: 'Position', 'Orientation' or 'Both'!";
			return null;
		}
    };

	//export
	XMOT.ClientMotionFactory = ClientMotionFactory;
}());(function(){
	/**
	 * CollisionConstraint
	 * @constructor
	 * @param {number} sceneWidth width of the scene to which the map applies
	 * @param {number} sceneDepth depth of the scene to which the map applies
	 * @param {Array.<number>} normal normal of the plane to the which the map applies
	 * @param {String} collisionMap URL of the CollisionMap
	 * @implements {Constraint}
	 */
	var CollisionConstraint = function(sceneWidth, sceneDepth, normal, collisionMap){
		/**
		 * Width of the scene
		 * @private
		 * @type {number}
		 */
		this.sceneWidth = sceneWidth;
		/**
		 * Depth (or height) of the scene
		 * @private
		 * @type {number}
		 */
		this.sceneDepth = sceneDepth;
		/** 
		 * Normal of the plan related to the collisionMap
		 * @private
		 * @type {Array.<number>}
		 */
		this.normal = normal;

		//draw the map to a canvas to be able to get pixel data
		/**
		 * Collisionmap
		 * @private
		 * @type {Image}
		 */
		this.img = new Image();
		this.img.src = collisionMap;
		/**
		 * Canvas, the image painted on this canvas in order to be able to address single pixels
		 * @private
		 * @type {Canvas}
		 */
		this.canvas = document.createElement("canvas");
		this.canvas.setAttribute("width", this.img.width);
		this.canvas.setAttribute("height", this.img.height);
		/**
		 * Context of the canvas
		 * @private
		 * @type {Context}
		 */
		this.context = this.canvas.getContext("2d");
		this.context.drawImage(this.img, 0, 0);

	};
	var c = CollisionConstraint.prototype;

	/** @inheritDoc */
    c.constrainRotation = function(rotation, moveable){
		//TODO: implement something useful
    	//COMMENT(rryk): How about coding rotation constraints in the RGB color? Just a crazy idea...
		return true;
    };

    /** @inheritDoc */
    c.constrainTranslation = function(translation, moveable){
		//TODO: check rotationssymmetrische dingsda, also z achse der szene = -y des bildes?
		//TODO: check normal
    	var currentPos = moveable.getPosition();
		var checkAtX = (currentPos[0] + translation[0] ) / this.sceneWidth * this.img.width;
		var checkAtY = (currentPos[2] + translation[2] ) / this.sceneDepth * this.img.height;
		if(!checkAtX) checkAtX = 0;
		if(!checkAtY) checkAtY = 0;
		var data = this.context.getImageData(checkAtX,checkAtY,1,1).data;
		return (data[0] || data[1] || data[2]);
    };

    //export
    XMOT.CollisionConstraint = CollisionConstraint;
}());
(function(){
	/**
	 * ConstraintCollection
	 * Combines a number of constraints
	 * @constructor
	 * @param {Array.<Constraint>} constraints
	 * @implements {Constraint}
	 */
	var ConstraintCollection = function(constraints){
		/**
		 * Collection of Contraints
		 * @private
		 * @type {Array.<Constraint>}
		 */
		this.constraints = constraints == undefined ? [] : constraints;
	};
	var c = ConstraintCollection.prototype;

	/** @inheritDoc */
    c.constrainRotation = function(rotation, moveable){
		var length = this.constraints.length;
		var i = 0;
		var ret = true;
		while(i<length && ret){
			//TODO: run over all constraints instead of a break as soon as a false is returned from of them?
			// this would allow all constraints to do something with the transformation or the moveable.
			// however, this might lead to a status in a which a change of a constraint changes
			// the behaviour of an following constraint
			ret = ret && this.constraints[i].constrainRotation(rotation, moveable);
			i++;
		}
    	return ret;
    };

    /** @inheritDoc */
    c.constrainTranslation = function(translation, moveable){
		var length = this.constraints.length;
		var i = 0;
		var ret = true;
		while(i<length && ret){
			ret = ret && this.constraints[i].constrainTranslation(translation, moveable);
			i++;
		}
    	return ret;
    };

    /**
     * Adds a constraint to the collection
     * @param {Constraint} constraint
     */
    c.addConstraint = function(constraint){
		this.constraints.push(constraint);
    };

    /**
     * Removes a constraint from the collection
     * @param {Constraint} constraint
     */
    c.removeContraint = function(constraint){
		var i = this.constraints.indexOf(constraint);
		//indexOf returns -1 if item was not found
		if(i !== -1) constraints.splice(i,1);
    };

    //export
    XMOT.ConstraintCollection = ConstraintCollection;
}());

(function(){

	/**
	 * An implementation of Animatable
	 * @constructor
	 * @implements Animatable
	 * @extends ClientMoveable
	 */
	var ClientAnimatable = function(obj, transform, constraint){

		//call parent constructor here
		goog.base(this, obj, transform, constraint);

		/**
		 * Map of KeyframeAnimations
		 * @private
		 * @type {string, {animation: Animation, opt: Object=}}
		 */
		this.availableAnimations = {};
		/**
		 * Map of active KeyframeAnimations
		 * Note: This works since the IDs are only numbers.
		 * Those numbers are turned into strings  and those are used as keys.
		 * @private
		 * @type {number, {animation: Animation, clockGenerator: TWEEN.Tween opt: Object=}}
		 */
		this.activeAnimations = {};
		/**
		 * Counter of IDs for active animations
		 * Attention: this might turn to infinity
		 * @private
		 * @type {number}
		 */
		this.idCounter = 0;
	};

	//inheritence is done here
	goog.inherits(ClientAnimatable, XMOT.ClientMoveable);

    var a = ClientAnimatable.prototype;

    /** @inheritDoc */
    a.addAnimation = function(animation, opt){
		//do not change options of the animation, store options of the animation of this animatable
		//same animation might have different options on another animatable
		this.availableAnimations[animation.name] = new Object();
		var tmp = this.availableAnimations[animation.name];
		tmp.opt = opt;
		tmp.animation = animation;
    };

    /** @inheritDoc */
    a.startAnimation = function(name, opt){
		var id = this.idCounter;
		this.idCounter++;
		this.activeAnimations[id] = {animation:this.availableAnimations[name].animation, opt:opt};
		this.startClockGenerator(id);
		//finally return the id after setting up everything
		return id;
    };

    /**
     * Starts a ClockGenerator which calls the Animation "from time to time", which then applies the current status of the animation to the animatable.
     * @private
     */
    a.startClockGenerator = function(id){
		//use a tween as a clock generator
		var time = this.checkOption("duration", id);
		var cg = new TWEEN.Tween({t:0}).to({t:time}, time).delay(this.checkOption("delay",id));

		//setup update and complete callbacks
		var that = this;
		cg.onUpdate(function(value){
			//this is the interpolated object!
			that.activeAnimations[id].animation.applyAnimation(that, this.t, 0, time, that.checkOption("easing", id));
		});

		cg.onComplete( function(value){
			//this is the interpolated object!
			//animation ended -> callback or loop
			var numberOfLoops = that.checkOption("loop", id);
			var animation = that.activeAnimations[id];
			if(isFinite(numberOfLoops)){
				if( numberOfLoops > 1 ){ //we must loop again
					if(animation.opt != undefined)
						animation.opt.loop = numberOfLoops - 1;
					else
						animation.opt = {loop: numberOfLoops-1};
					that.startClockGenerator(id);
				}else {
					//no more loops, we are finished and now the callback
					var cb = that.checkOption("callback", id);
					if(typeof(cb) === "function") cb();
					animation = {}; //clean up
				}
			}
			else{
				//infinite loops
				that.startClockGenerator(id);
			}
		});

		//and finally the start
		this.activeAnimations[id].clockGenerator = cg;
		cg.start();
		if(!XMOT.animating) {
			XMOT.animating = true;
			XMOT.animate();
		}
    };

    /** @inheritDoc */
    a.stopAnimation = function(id){
		//stop animation
		this.activeAnimations[id].clockGenerator.stop();
		//delete from map - TODO how to do this correctly?
		this.activeAnimations[id] = {};
		return this;
    };

	/**
	 * Checks for a single options and returns the correct value according to the hierachy of different opts
	 * @param {string} name name of the option
	 * @param {number} animationID
	 */
	a.checkOption = function(name, animationID){
		//TODO: make the lib more efficient by filling options in the add/ start function
		//but this will also make the code less readable
		var startOpt = this.activeAnimations[animationID].opt;
		if(startOpt != undefined && startOpt[name] != undefined){
			return startOpt[name];
		}
		else {
			//options provided while adding the animation to the animatable
			var animationOpt = this.availableAnimations[this.activeAnimations[animationID].animation.name].opt;
			if(animationOpt != undefined && animationOpt[name] != undefined){
				return animationOpt[name];
			}else
				//option of the animation itself
				return this.activeAnimations[animationID].animation.getOption(name);
		}
	};

    //export
	XMOT.ClientAnimatable = ClientAnimatable;

}());(function(){
	/**
	 * ClientKeyframeAnimation implementation
	 * @param{string} name name of the animation
	 * @param{Array.<number>} keys keys
	 * @param{Array.<number>|undefined} positionValues
	 * @param{Array.<number>|undefined} orientationValues
	 * @constructor
	 * @implements{MotionFactory}
	 */
	function ClientKeyframeAnimation(name, keys, positionValues, orientationValues, opt){

		/**
		 * name of animation
		 * @private
		 * @type {string}
		 */
		this.name = name;
		/**
		 * Array of the keys
		 * @private
		 * @type{Array.<number>}
		 */
		this.keys = keys;
		/**
		 * Array fo the position values
		 * @private
		 * @type{Array.<number>|undefined}
		 */
		this.positionValues = positionValues;
		/**
		 * Array of the orientation values
		 * @private
		 * @type{Array.<number>|undefined}
		 */
		this.orientationValues = orientationValues;
		
		//options - set defaults
		/**
		 * loop
		 * @private
		 * @type {number}
		 */
		this.loop = 1;
		/**
		 * delay
		 * @private
		 * @type{number}
		 */
		this.delay = 0;
		/**
		 * Duration of the animation
		 * @private
		 * @type {number}
		 */
		this.duration = 1000;
		/**
		 * easing
		 * @private
		 * @type {function}
		 */
		this.easing = TWEEN.Easing.Linear.None;
		/**
		 * Callback, executed as soon as the animation ended
		 * @private
		 * @type {function}
		 */
		this.callback = function(){};
		if(opt){
			this.setOptions(opt);
		}
	};

	var k = ClientKeyframeAnimation.prototype;

	/** @inheritDoc */
	k.applyAnimation = function(animatable, currentTime, startTime, endTime, easing){
		var t = (currentTime - startTime) / (endTime - startTime);
		if(easing && typeof(easing) === "function") t = easing(t); //otherwise its linear
		var l = this.keys.length - 1;
		if (t <= this.keys[0]){
			this.setValue( animatable, this.getPosition(0), this.getOrientation(0) );
		}else if (t >= this.keys[l - 1]){
			this.setValue( animatable, this.getPosition(l), this.getOrientation(l) );
		}else{
			for ( var i = 0; i < l - 1; i++){
				if (this.keys[i] < t && t <= this.keys[i + 1]) {
					var p = (t - this.keys[i]) / (this.keys[i + 1] - this.keys[i]);
					this.setValue( animatable, this.getInterpolatedPosition(i, p), this.getInterpolatedOrientation(i, p) );
				}
			}
		}
	};

	/**
	 * Set position and animation of the animatable
	 * @private
	 * @param {Array.<number>|undefined}
	 * @param {Array.<number>|undefined}
	 */
	k.setValue = function(animatable, position, orientation){
		if(position != undefined)
			animatable.setPosition(position);
		if(orientation != undefined)
			animatable.setOrientation(orientation);
	};

	/**
	 * Interpolates keyvalues between index i and index i+1 with parameter t
	 * @private
	 * @param {number} index
	 * @param {number} t interpolationparameter
	 * @return {Array.<number>} Position
	 */
	k.getInterpolatedPosition = function(index, t){
		if(this.positionValues == undefined) return undefined;
		var ret = [];
		var start = this.getPosition(index);
		var end = this.getPosition(index+1);
		var i = 0;
		for(i=0; i<start.length; i++ ){
			ret[i] = start[i] + ( end[i] - start[i] ) * t;
		}
		return ret;
	};

	/**
	 * Interpolates keyvalues between index i and index i+1 with parameter t
	 * @private
	 * @param {number} index
	 * @param {number} t interpolationparameter
	 * @return {Array.<number>} Orientation
	 */
	k.getInterpolatedOrientation = function(index, t){
		if(this.orientationValues == undefined) return undefined;
		var start = this.getOrientation(index);
		var end = this.getOrientation(index+1);
		//the newely created quat gets filled with the result and returned
		return quat4.slerp(start, end, t, quat4.create());
	};

	/**
	 * Gets a position corresponding to a key
	 * @private
	 * @param {number} key
	 * @return {Array.<number>} Position
	 */
	k.getPosition = function(key){
		if(this.positionValues == undefined || key > this.keys.length-1 /*just in case*/) return undefined;
		var index = key*3;
		return [ this.positionValues[index], this.positionValues[index+1], this.positionValues[index+2] ];
	};

	/**
	 * Gets an orientation corresponding to a key
	 * @private
	 * @param {number} key
	 * @return {Array.<number>} Orientation
	 */
	k.getOrientation = function(key){
		if(this.orientationValues == undefined || key > this.keys.length-1 /*just in case*/) return undefined;
		var index = key*4;
		return [ this.orientationValues[index], this.orientationValues[index+1], this.orientationValues[index+2], this.orientationValues[index+3] ];
	};

	/** @inheritDoc */
	k.getOption = function(name){
		return this[name];
	};

    /** @inheritDoc */
    k.setOptions = function(opt){
		if(opt.loop)
			this.loop = opt.loop;
		if(opt.duration)
			this.duration = opt.duration;
		if(opt.easingk && typeof(opt.easing) === "function")
			this.easing = opt.easing;
		if(opt.callback && typeof(opt.callback) === "function")
			this.callback = opt.callback;
    };

	//export
	XMOT.ClientKeyframeAnimation = ClientKeyframeAnimation;
}());(function(){
	/**
	 * SimpleConstraint
	 * @constructor
	 * @param {Boolean} allowedToMove
	 * @implements {Constraint}
	 */
	var SimpleConstraint = function(allowedToMove){
		/**
		 * The value wich is returned everytime
		 * @private
		 * @type {Boolean}
		 */
		this.allowedToMove = allowedToMove;
	};
	var s = SimpleConstraint.prototype;

	/** @inheritDoc */
    s.constrainRotation = function(rotation, moveable){
		return this.allowedToMove;
    };

    /** @inheritDoc */
    s.constrainTranslation = function(translation, moveable){
		return this.allowedToMove;
    };

    //export
    XMOT.SimpleConstraint = SimpleConstraint;
}());
(function(){
	/**
	 * ProhibitAxisMovementConstraint
	 * prohibit axismovement, but allow movement around an epsilon of a specified center
	 * @constructor
	 * @param {Boolean} x prohibit x axis
	 * @param {Boolean} y prohibit y axis
	 * @param {Boolean} z prohibit z axis
	 * @param {number} epsilon
	 * @param {center} epsilon
	 * @implements {Constraint}
	 */
	var ProhibitAxisMovementConstraint = function(x,y,z, epsilon, center){
		/**
		 * prohibit x axis
		 * @private
		 * @type {Boolean}
		 */
		this.x = x;
		/**
		 * prohibit y axis
		 * @private
		 * @type {Boolean}
		 */
		this.y = y;
		/**
		 * prohibit z axis
		 * @private
		 * @type {Boolean}
		 */
		this.z = z;
		this.epsilon = epsilon ? epsilon : 0;
		this.center =  center ? center : 0;

	};
	var c = ProhibitAxisMovementConstraint.prototype;

	/** @inheritDoc */
    c.constrainRotation = function(rotation, moveable){
		return true;
    };

    /** @inheritDoc */
    c.constrainTranslation = function(translation, moveable){
    	if(this.x && Math.abs(this.center - (moveable.getPosition()[0]+translation[0])) > this.epsilon) translation[0] = 0;
    	if(this.y && Math.abs(this.center - (moveable.getPosition()[1]+translation[1])) > this.epsilon) translation[1] = 0;
    	if(this.z && Math.abs(this.center - (moveable.getPosition()[2]+translation[2])) > this.epsilon) translation[2] = 0;
    	return true;
    };

    //export
    XMOT.ProhibitAxisMovementConstraint = ProhibitAxisMovementConstraint;
}());

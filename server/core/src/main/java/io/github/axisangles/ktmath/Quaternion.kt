@file:Suppress("unused")

package io.github.axisangles.ktmath

import kotlin.math.*

data class Quaternion(val w: Float, val x: Float, val y: Float, val z: Float) {
	companion object {
		val NULL = Quaternion(0f, 0f, 0f, 0f)
		val IDENTITY = Quaternion(1f, 0f, 0f, 0f)
		val I = Quaternion(0f, 1f, 0f, 0f)
		val J = Quaternion(0f, 0f, 1f, 0f)
		val K = Quaternion(0f, 0f, 0f, 1f)

		/**
		 * creates a new quaternion representing the rotation about v's axis
		 * by an angle of v's length
		 * @param v the rotation vector
		 * @return the new quaternion
		 **/
		fun fromRotationVector(v: Vector3): Quaternion = Quaternion(0f, v / 2f).exp()

		/**
		 * creates a new quaternion representing the rotation about axis v
		 * by an angle of v's length
		 * @param vx the rotation vector's x component
		 * @param vy the rotation vector's y component
		 * @param vz the rotation vector's z component
		 * @return the new quaternion
		 **/
		fun fromRotationVector(vx: Float, vy: Float, vz: Float): Quaternion =
			fromRotationVector(Vector3(vx, vy, vz))

		/**
		 * finds Q, the smallest-angled quaternion whose local u direction aligns with
		 * the global v direction.
		 * @param u the local direction
		 * @param v the global direction
		 * @return Q
		 **/
		fun fromTo(u: Vector3, v: Vector3): Quaternion {
			val U = Quaternion(0f, u)
			val V = Quaternion(0f, v)
			val D = V / U

			return (D + D.len()).unit()
		}
	}

	/**
	 * @return the quaternion with w real component and xyz imaginary components
	 */
	constructor(w: Float, xyz: Vector3) : this(w, xyz.x, xyz.y, xyz.z)

	/**
	 * @return the imaginary components as a vector3
	 **/
	val xyz get(): Vector3 = Vector3(x, y, z)

	/**
	 * @return the quaternion with only the w component
	 **/
	val re get(): Quaternion = Quaternion(w, 0f, 0f, 0f)

	/**
	 * @return the quaternion with only x y z components
	 **/
	val im get(): Quaternion = Quaternion(0f, x, y, z)

	operator fun unaryMinus(): Quaternion = Quaternion(-w, -x, -y, -z)

	operator fun plus(that: Quaternion): Quaternion = Quaternion(
		this.w + that.w,
		this.x + that.x,
		this.y + that.y,
		this.z + that.z
	)

	operator fun plus(that: Float): Quaternion =
		Quaternion(this.w + that, this.x, this.y, this.z)

	operator fun minus(that: Quaternion): Quaternion = Quaternion(
		this.w - that.w,
		this.x - that.x,
		this.y - that.y,
		this.z - that.z
	)

	operator fun minus(that: Float): Quaternion =
		Quaternion(this.w - that, this.x, this.y, this.z)

	/**
	 * computes the dot product of this quaternion with that quaternion
	 * @param that the quaternion with which to be dotted
	 * @return the inverse quaternion
	 **/
	fun dot(that: Quaternion): Float =
		this.w * that.w + this.x * that.x + this.y * that.y + this.z * that.z

	/**
	 * computes the square of the length of this quaternion
	 * @return the length squared
	 **/
	fun lenSq(): Float = w * w + x * x + y * y + z * z

	/**
	 * computes the length of this quaternion
	 * @return the length
	 **/
	fun len(): Float = sqrt(w * w + x * x + y * y + z * z)

	/**
	 * @return the normalized quaternion
	 **/
	fun unit(): Quaternion {
		val m = len()
		return if (m == 0f) NULL else (this / m)
	}

	operator fun times(that: Float): Quaternion = Quaternion(
		this.w * that,
		this.x * that,
		this.y * that,
		this.z * that
	)

	operator fun times(that: Quaternion): Quaternion = Quaternion(
		this.w * that.w - this.x * that.x - this.y * that.y - this.z * that.z,
		this.x * that.w + this.w * that.x - this.z * that.y + this.y * that.z,
		this.y * that.w + this.z * that.x + this.w * that.y - this.x * that.z,
		this.z * that.w - this.y * that.x + this.x * that.y + this.w * that.z
	)

	/**
	 * computes the inverse of this quaternion
	 * @return the inverse quaternion
	 **/
	fun inv(): Quaternion {
		val lenSq = lenSq()
		return Quaternion(
			w / lenSq,
			-x / lenSq,
			-y / lenSq,
			-z / lenSq
		)
	}

	operator fun div(that: Float): Quaternion = this * (1f / that)

	/**
	 * computes right division, this * that^-1
	 **/
	operator fun div(that: Quaternion): Quaternion = this * that.inv()

	/**
	 * @return the conjugate of this quaternion
	 **/
	fun conj(): Quaternion = Quaternion(w, -x, -y, -z)

	/**
	 * computes the logarithm of this quaternion
	 * @return the log of this quaternion
	 **/
	fun log(): Quaternion {
		val co = w
		val si = xyz.len()
		val len = len()

		if (si == 0f) {
			return Quaternion(ln(len), xyz / w)
		}

		val ang = atan2(si, co)
		return Quaternion(ln(len), ang / si * xyz)
	}

	/**
	 * raises e to the power of this quaternion
	 * @return the exponentiated quaternion
	 **/
	fun exp(): Quaternion {
		val ang = xyz.len()
		val len = exp(w)

		if (ang == 0f) {
			return Quaternion(len, len * xyz)
		}

		val co = cos(ang)
		val si = sin(ang)
		return Quaternion(len * co, len * si / ang * xyz)
	}

	/**
	 * raises this quaternion to the power of t
	 * @param t the power by which to raise this quaternion
	 * @return the powered quaternion
	 **/
	fun pow(t: Float): Quaternion = (log() * t).exp()

	/**
	 * between this and -this, picks the one nearest to that quaternion
	 * @param that the quaternion to be nearest
	 * @return nearest quaternion
	 **/
	fun twinNearest(that: Quaternion): Quaternion =
		if (this.dot(that) < 0f) -this else this

	/**
	 * interpolates from this quaternion to that quaternion by t in quaternion space
	 * @param that the quaternion to interpolate to
	 * @param t the amount to interpolate
	 * @return interpolated quaternion
	 **/
	fun interpQ(that: Quaternion, t: Float) =
		if (t == 0f) {
			this
		} else if (t == 1f) {
			that
		} else if (t < 0.5f) {
			(that / this).pow(t) * this
		} else {
			(this / that).pow(1f - t) * that
		}

	/**
	 * interpolates from this quaternion to that quaternion by t in rotation space
	 * @param that the quaternion to interpolate to
	 * @param t the amount to interpolate
	 * @return interpolated quaternion
	 **/
	fun interpR(that: Quaternion, t: Float) = this.interpQ(that.twinNearest(this), t)

	/**
	 * linearly interpolates from this quaternion to that quaternion by t in
	 * quaternion space
	 * @param that the quaternion to interpolate to
	 * @param t the amount to interpolate
	 * @return interpolated quaternion
	 **/
	fun lerpQ(that: Quaternion, t: Float): Quaternion = (1f - t) * this + t * that

	/**
	 * linearly interpolates from this quaternion to that quaternion by t in
	 * rotation space
	 * @param that the quaternion to interpolate to
	 * @param t the amount to interpolate
	 * @return interpolated quaternion
	 **/
	fun lerpR(that: Quaternion, t: Float) = this.lerpQ(that.twinNearest(this), t)

	/**
	 * computes this quaternion's angle to identity in quaternion space
	 * @return angle
	 **/
	fun angleQ(): Float = atan2(xyz.len(), w)

	/**
	 * computes this quaternion's angle to identity in rotation space
	 * @return angle
	 **/
	fun angleR(): Float = 2f * atan2(xyz.len(), abs(w))

	/**
	 * computes the angle between this quaternion and that quaternion in quaternion space
	 * @param that the other quaternion
	 * @return angle
	 **/
	fun angleToQ(that: Quaternion): Float = (this / that).angleQ()

	/**
	 * computes the angle between this quaternion and that quaternion in rotation space
	 * @param that the other quaternion
	 * @return angle
	 **/
	fun angleToR(that: Quaternion): Float = (this / that).angleR()

	/**
	 * computes the angle this quaternion rotates about the u axis in quaternion space
	 * @param u the axis
	 * @return angle
	 **/
	fun angleAboutQ(u: Vector3): Float {
		val si = u.dot(xyz)
		val co = u.len() * w
		return atan2(si, co)
	}

	/**
	 * computes the angle this quaternion rotates about the u axis in rotation space
	 * @param u the axis
	 * @return angle
	 **/
	fun angleAboutR(u: Vector3): Float = 2f * twinNearest(IDENTITY).angleAboutQ(u)

	/**
	 * finds Q, the quaternion nearest to this quaternion representing a rotation purely
	 * about the global u axis. Q is NOT unitized
	 * @param v the global axis
	 * @return Q
	 **/
	fun project(v: Vector3) = Quaternion(w, xyz.dot(v) / v.lenSq() * v)

	/**
	 * finds Q, the quaternion nearest to this quaternion representing a rotation NOT
	 * on the global u axis. Q is NOT unitized
	 * @param v the global axis
	 * @return Q
	 **/
	fun reject(v: Vector3) = Quaternion(w, v.cross(xyz).cross(v) / v.lenSq())

	/**
	 * finds Q, the quaternion nearest to this quaternion whose local u direction aligns
	 * with the global v direction. Q is NOT unitized
	 * @param u the local direction
	 * @param v the global direction
	 * @return Q
	 **/
	fun align(u: Vector3, v: Vector3): Quaternion {
		val U = Quaternion(0f, u)
		val V = Quaternion(0f, v)

		return (V * this / U + (V / U).len() * this) / 2f
	}

	/**
	 * applies this quaternion's rotation to that vector
	 * @param that the vector to be transformed
	 * @return that vector transformed by this quaternion
	 **/
	fun sandwich(that: Vector3): Vector3 = (this * Quaternion(0f, that) / this).xyz

	/**
	 * computes this quaternion's unit length rotation axis
	 * @return rotation axis
	 **/
	fun axis(): Vector3 = xyz.unit()

	/**
	 * computes the rotation vector representing this quaternion's rotation
	 * @return rotation vector
	 **/
	fun toRotationVector(): Vector3 = 2f * twinNearest(IDENTITY).log().xyz

	/**
	 * computes the matrix representing this quaternion's rotation
	 * @return rotation matrix
	 **/
	fun toMatrix(): Matrix3 {
		val d = lenSq()
		/* ktlint-disable */
		return Matrix3(
			(w*w + x*x - y*y - z*z)/d ,      2f*(x*y - w*z)/d     ,      2f*(w*y + x*z)/d     ,
			     2f*(x*y + w*z)/d     , (w*w - x*x + y*y - z*z)/d ,      2f*(y*z - w*x)/d     ,
			     2f*(x*z - w*y)/d     ,      2f*(w*x + y*z)/d     , (w*w - x*x - y*y + z*z)/d )
		/* ktlint-enable */
	}

	/**
	 * computes the euler angles representing this quaternion's rotation
	 * @param order the order in which to decompose this quaternion into euler angles
	 * @return euler angles
	 **/
	fun toEulerAngles(order: EulerOrder): EulerAngles =
		this.toMatrix().toEulerAnglesAssumingOrthonormal(order)
}

operator fun Float.plus(that: Quaternion): Quaternion = that + this
operator fun Float.minus(that: Quaternion): Quaternion = -that + this
operator fun Float.times(that: Quaternion): Quaternion = that * this
operator fun Float.div(that: Quaternion): Quaternion = that.inv() * this

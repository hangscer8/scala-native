package scala.scalanative

import native._
import runtime.Intrinsics._

package object runtime {

  /** Runtime Type Information. */
  type Type = CStruct3[Int, String, Byte]

  implicit class TypeOps(val self: Ptr[Type]) extends AnyVal {
    def id: Int      = !(self._1)
    def name: String = !(self._2)
    def kind: Long   = !(self._3)
  }

  /** Class runtime type information. */
  type ClassType = CStruct3[Type, Long, CStruct2[Int, Int]]

  implicit class ClassTypeOps(val self: Ptr[ClassType]) extends AnyVal {
    def id: Int           = self._1.id
    def name: String      = self._1.name
    def kind: Long        = self._1.kind
    def size: Long        = !(self._2)
    def idRangeFrom: Long = !(self._3._1)
    def idRangeTo: Long   = !(self._3._2)
  }

  final val CLASS_KIND  = 0
  final val TRAIT_KIND  = 1
  final val STRUCT_KIND = 2

  /** Used as a stub right hand of intrinsified methods. */
  def intrinsic: Nothing = throwUndefined()

  /** Returns info pointer for given type. */
  def typeof[T](implicit tag: Tag[T]): Ptr[Type] = intrinsic

  /** Intrinsified unsigned devision on ints. */
  def divUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned devision on longs. */
  def divULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified unsigned remainder on ints. */
  def remUInt(l: Int, r: Int): Int = intrinsic

  /** Intrinsified unsigned remainder on longs. */
  def remULong(l: Long, r: Long): Long = intrinsic

  /** Intrinsified byte to unsigned int converstion. */
  def byteToUInt(b: Byte): Int = intrinsic

  /** Intrinsified byte to unsigned long conversion. */
  def byteToULong(b: Byte): Long = intrinsic

  /** Intrinsified short to unsigned int conversion. */
  def shortToUInt(v: Short): Int = intrinsic

  /** Intrinsified short to unsigned long conversion. */
  def shortToULong(v: Short): Long = intrinsic

  /** Intrinsified int to unsigned long conversion. */
  def intToULong(v: Int): Long = intrinsic

  /** Read type information of given object. */
  def getType(obj: Object): Ptr[ClassType] = !obj.cast[Ptr[Ptr[ClassType]]]

  /** Get monitor for given object. */
  def getMonitor(obj: Object): Monitor = Monitor.dummy

  /** Initialize runtime with given arguments and return the
   *  rest as Java-style array.
   */
  def init(argc: Int, argv: Ptr[Ptr[Byte]]): scala.Array[String] = {
    val args = new scala.Array[String](argc - 1)

    // skip the executable name in argv(0)
    var c = 0
    while (c < argc - 1) {
      // use the default Charset (UTF_8 atm)
      args(c) = fromCString(argv(c + 1))
      c += 1
    }

    args
  }

  /** Run the runtime's event loop. The method is called from the
   *  generated C-style after the application's main method terminates.
   */
  def loop(): Unit =
    ExecutionContext.loop()

  /** Called by the generated code in case of division by zero. */
  @noinline def throwDivisionByZero(): Nothing =
    throw new java.lang.ArithmeticException("/ by zero")

  /** Called by the generated code in case of incorrect class cast. */
  @noinline def throwClassCast(from: Ptr[Type], to: Ptr[Type]): Nothing =
    throw new java.lang.ClassCastException(
      s"${!from._1} cannot be cast to ${!to._1}")

  /** Called by the generated code in case of operations on null. */
  @noinline def throwNullPointer(): Nothing =
    throw new NullPointerException()

  /** Called by the generated code in case of unexpected condition. */
  @noinline def throwUndefined(): Nothing =
    throw new UndefinedBehaviorError
}

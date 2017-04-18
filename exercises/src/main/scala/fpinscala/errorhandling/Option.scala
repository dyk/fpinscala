package fpinscala.errorhandling


import scala.{Option => _, Some => _, Either => _, _} // hide std library `Option`, `Some` and `Either`, since we are writing our own in this chapter

sealed trait Option[+A] {
  def map[B](f: A => B): Option[B]

  def getOrElse[B>:A](default: => B): B

  def flatMap[B](f: A => Option[B]): Option[B]

  def orElse[B>:A](ob: => Option[B]): Option[B]

  def filter(f: A => Boolean): Option[A]
}
case class Some[+A](get: A) extends Option[A] {
  override def map[B](f: (A) => B): Option[B] = Some(f(get))
  override def getOrElse[B >: A](default: => B): B = get
  override def flatMap[B](f: (A) => Option[B]): Option[B] = f(get)
  override def orElse[B >: A](ob: => Option[B]): Option[B] = this
  override def filter(f: (A) => Boolean): Option[A] = if (f(get)) this else None
}
case object None extends Option[Nothing] {
  override def map[B](f: (Nothing) => B): Option[B] = None
  override def getOrElse[B >: Nothing](default: => B): B = default
  override def flatMap[B](f: (Nothing) => Option[B]): Option[B] = None
  override def orElse[B >: Nothing](ob: => Option[B]): Option[B] = ob
  override def filter(f: (Nothing) => Boolean): Option[Nothing] = None
}

object Option {
  def failingFn(i: Int): Int = {
    val y: Int = throw new Exception("fail!") // `val y: Int = ...` declares `y` as having type `Int`, and sets it equal to the right hand side of the `=`.
    try {
      val x = 42 + 5
      x + y
    }
    catch { case e: Exception => 43 } // A `catch` block is just a pattern matching block like the ones we've seen. `case e: Exception` is a pattern that matches any `Exception`, and it binds this value to the identifier `e`. The match returns the value 43.
  }

  def failingFn2(i: Int): Int = {
    try {
      val x = 42 + 5
      x + ((throw new Exception("fail!")): Int) // A thrown Exception can be given any type; here we're annotating it with the type `Int`
    }
    catch { case e: Exception => 43 }
  }

  def mean(xs: Seq[Double]): Option[Double] =
    if (xs.isEmpty) None
    else Some(xs.sum / xs.length)
  def variance(xs: Seq[Double]): Option[Double] =
    mean(xs).flatMap( m => mean(xs.map( x => math.pow(x-m, 2))))

  def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] = a.flatMap( a1 => b.map( b1 => f(a1,b1)) )

  def sequence[A](a: List[Option[A]]): Option[List[A]] = a.foldLeft[Option[List[A]]](Some(Nil))(map2(_, _)( (acc, x) => x :: acc ))

  def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] = a.foldLeft[Option[List[B]]](Some(Nil))( (z, x) => map2(z, f(x))( (acc, fx) => fx :: acc ))
}
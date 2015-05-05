package org.broadinstitute.dsde.vault.common.util

import org.scalatest.{FreeSpec, Matchers}

class ImplicitMagnetTestSpec extends FreeSpec with Matchers {
  "ImplicitMagnet" - {
    "when using implicit magnets" - {

      class Foo {
        def internalValue = "foo"
      }

      class Bar extends Foo {
        override def internalValue = "bar"
      }

      def getMagnetValue(magnet: ImplicitMagnet[Foo]): String = magnet.value.internalValue

      "testing basic implicit should return value" in {
        implicit val f = new Foo
        val magnetValue = getMagnetValue(()) // http://stackoverflow.com/a/25354165/3320205
        magnetValue should be("foo")
      }
      "testing child implicit should return value" in {
        implicit val b = new Bar
        val magnetValue = getMagnetValue(()) // http://stackoverflow.com/a/25354165/3320205
        magnetValue should be("bar")
      }
    }
  }
}

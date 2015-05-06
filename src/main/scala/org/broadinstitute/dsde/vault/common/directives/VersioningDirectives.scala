package org.broadinstitute.dsde.vault.common.directives

import shapeless.{HList, ::, HNil}
import spray.routing.{PathMatcher, Directive}
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.PathDirectives._

object VersioningDirectives {
  val optionalVersion: Directive[Option[Int] :: HNil] =
    pathPrefix("v" ~ IntNumber).map(Option(_)) | provide(None)

  def pathPrefixVersion(prefix: String) = pathPrefix(prefix) & optionalVersion
  def pathVersion(prefix: String) = pathPrefixVersion(prefix) & pathEnd
  def pathVersion[L <: HList](prefix: String, pm: PathMatcher[L]) = pathPrefixVersion(prefix) & path(pm)

  /*
  TODO: can't manage scala-fu for syntax to compile
  def pathPrefixVersion[L](prefix: String, pm: PathMatcher[L]) = pathPrefix(prefix) & optionalVersion & pathPrefix(pm)

  Ideal API allows optional version to appear after the first segment.

  Example prefixes:
    /head/v1/123, /head/v2/123, or just /head/123

  Preferable signature, with pseudo implementation:
    pathPrefixVersion(pm1: PathMatcher, pm2: PathMatcher) = pathPrefix(pm1) & optionalVersion & pathPrefix(pm2)
  Acceptable signature:
    pathPrefixVersion(prefix: String, pm: PathMatcher) = pathPrefix(prefix) & optionalVersion & pathPrefix(pm)

  If one manages to get a PathMatcher that optional matches a version you win extra points! Maybe with another regex?

    pathPrefix("pre" / OptionalVersion / IntNumber / Segment) => { (optionalVersionVal, intVal, stringVal) => ... }

  Other examples of syntax debugging:
  Works:        def path2[L, R](pm1: PathMatcher[L], pm2: PathMatcher[R]) = pathPrefix(pm1 ~ pm2)
  Doesn't work: def path2[L, R](pm1: PathMatcher[L], pm2: PathMatcher[R]) = pathPrefix(pm1) & pathPrefix(pm2)

  Also debugged for a while with flatMap/hflatMap and map/hmap, using provide/hprovide.
  */
}

package org.broadinstitute.dsde.vault.common.directives

import shapeless.{::, HList, HNil}
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.PathDirectives._
import spray.routing.{Directive, PathMatcher}

/**
 * Handles optional URI versioning in routes, providing backwards compatibility.
 *
 * Sometimes routes originally handle only `/hello`, but later need versioning as `/hello/v1`, `/hello/v2`, etc.
 * Use these directives so that versioned and unversioned requests are processed by the same routes.
 *
 * The core directive, `optionalVersion`, matches via a `[[spray.routing.PathMatcher PathMatcher]]` for `"v" ~ IntNumber`.
 *
 * Use the directives that take a single prefix `String` to handle basic paths such as "/hello/v1".
 * {{{
 *   route = put { pathOptionalVersion("hello") { version => complete(version) }
 *   PUT /hello/v1 --> Some(1)
 *   PUT /hello --> None
 * }}}
 *
 * When the path is followed by a other path parts, use the overloads that allow for additional `PathMatcher`s.
 * {{{
 *   route = get { pathOptionalVersion("hello", Segment) { (version, id) => complete((version, id)) }
 *   GET /hello/v1/my-id-here --> (Some(1), "my-id-here")
 *   GET /hello/my-id-here --> (None, "my-id-here")
 * }}}
 *
 * When needing to version add a new version, a `v2` endpoint may be added, leaving backwards compatibility for `v1`,
 * or URI without versions at all.
 * {{{
 *   route = get { pathOptionalVersion("hello", Segment) { (version, id) => complete((version, id)) }
 *   GET /hello/v2/my-id-here --> (Some(2), "my-id-here")
 *   GET /hello/v1/my-id-here --> (Some(1), "my-id-here")
 *   GET /hello/my-id-here --> (None, "my-id-here")
 * }}}
 *
 * The are other directives that also allow specifying the default version, to allow for easier matching, as they return an `Int` instead of `Option[Int]`.
 * {{{
 *   route = get { pathVersion("hello", 1, Segment) { (version, id) => complete((version, id)) }
 *   GET /hello/v2/my-id-here --> (2, "my-id-here")
 *   GET /hello/v1/my-id-here --> (1, "my-id-here")
 *   GET /hello/my-id-here --> (1, "my-id-here")
 * }}}
 *
 * These directives are composed from various directives in [[http://spray.io/documentation/1.2.3/spray-routing/path-directives/#pathdirectives spray-routing]],
 * especially `[[spray.routing.directives.PathDirectives!.pathPrefix pathPrefix()]]` and `[[spray.routing.directives.PathDirectives!.path path()]]`.
 *
 * To see a full list of usage examples, see the tests in `VersioningDirectivesSpec`.
 *
 * '''NOTE:''' If you are versioning directives from scratch, without needing optional versioning in URI routes,
 * it is recommended to use standard `spray-routing` directives such as `path("hello" / "v" ~ IntNumber / Segment)`,
 * forcing clients to explicitly pass a version.
 *
 * @see [[spray.routing.directives.PathDirectives!.pathPrefix pathPrefix()]]
 * @see [[spray.routing.directives.PathDirectives!.path path()]]
 */
object VersioningDirectives {
  /**
   * Directive providing an optional `v[version]` from the path as `Some[Int]` if found, or `None` if not found.
   */
  val optionalVersion: Directive[Option[Int] :: HNil] = pathPrefix("v" ~ IntNumber).map(Option(_)) | provide(None)

  /**
   * Matches an optional `v[version]` from the path, or returns the `default`.
   *
   * @param default Version to return if not found.
   * @return Directive providing the version in the path if found, or `default` if not found.
   */
  def defaultVersion(default: Int): Directive[Int :: HNil] = optionalVersion.map(_.getOrElse(default))

  /**
   * After a `prefix`, matches an optional `v[version]` from the path as `Some[Int]` if found, or `None` if not found.
   *
   * @param prefix Starting prefix of the path that must match.
   * @return Directive providing version in the path as `Some[Int]` if found, or `None` if not found.
   */
  def pathPrefixOptionalVersion(prefix: String): Directive[Option[Int] :: HNil] = pathPrefix(prefix) & optionalVersion

  /**
   * After a `prefix`, matches an optional `v[version]` from the path as `Some[Int]` if found, or `None` if not found, after which the path must end.
   *
   * @param prefix Starting prefix of the path that must match.
   * @return Directive providing the version in the path as `Some[Int]` if found, or `None` if not found.
   */
  def pathOptionalVersion(prefix: String): Directive[Option[Int] :: HNil] = pathPrefixOptionalVersion(prefix) & pathEnd

  /**
   * After a `prefix`, matches an optional `v[version]` from the path as `Some[Int]` if found, or `None` if not found, after which the path must end with `pm`.
   *
   * @param prefix Starting prefix of the path that must match.
   * @param pm Path matches to return after the version.
   * @return Directive providing the version in the path as `Some[Int]` if found, or `None` if not found, followed by other matches from `pm`.
   */
  def pathOptionalVersion[L <: HList](prefix: String, pm: PathMatcher[L]): Directive[Option[Int] :: L] = pathPrefixOptionalVersion(prefix) & path(pm)

  /**
   * After a `prefix`, matches an optional `v[version]` from the path, or a returns the `default`.
   *
   * @param prefix Starting prefix of the path that must match.
   * @param default Version to return if not found.
   * @return Directive providing the version in the path if found, or `default` if not found.
   */
  def pathPrefixVersion(prefix: String, default: Int): Directive[Int :: HNil] = pathPrefix(prefix) & defaultVersion(default)

  /**
   * After a `prefix`, matches an optional `v[version]` from the path, or a returns the `default`, after which the path must end.
   *
   * @param prefix Starting prefix of the path that must match.
   * @param default Version to return if not found.
   * @return Directive providing the version in the path if found, or `default` if not found.
   */
  def pathVersion(prefix: String, default: Int): Directive[Int :: HNil] = pathPrefixVersion(prefix, default) & pathEnd

  /**
   * After a `prefix`, matches an optional `v[version]` from the path, or a returns the `default`, after which the path must end with `pm`.
   *
   * @param prefix Starting prefix of the path that must match.
   * @param default Version to return if not found.
   * @param pm Path matches to return after the version.
   * @return Directive providing the version in the path if found, or `default` if not found, followed by other matches from `pm`.
   */
  def pathVersion[L <: HList](prefix: String, default: Int, pm: PathMatcher[L]): Directive[Int :: L] = pathPrefixVersion(prefix, default) & path(pm)

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

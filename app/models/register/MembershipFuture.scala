/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.register

import utils.{Enumerable, InputOption, WithName}

sealed trait MembershipFuture

object MembershipFuture {

  case object None extends WithName("opt1") with MembershipFuture
  case object One extends WithName("opt2") with MembershipFuture
  case object TwoToEleven extends WithName("opt3") with MembershipFuture
  case object TwelveToFifty extends WithName("opt4") with MembershipFuture
  case object FiftyOneToTenThousand extends WithName("opt5") with MembershipFuture
  case object MoreThanTenThousand extends WithName("opt6") with MembershipFuture

  val values: Seq[MembershipFuture] = Seq(
    None, One, TwoToEleven, TwelveToFifty,FiftyOneToTenThousand, MoreThanTenThousand
  )

  val options: Seq[InputOption] = values.map {
    value =>
      InputOption(value.toString,  s"messages__membership__${value.toString}")
  }

  implicit val enumerable: Enumerable[MembershipFuture] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
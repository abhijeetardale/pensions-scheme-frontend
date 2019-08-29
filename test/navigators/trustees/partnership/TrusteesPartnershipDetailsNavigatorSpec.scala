/*
 * Copyright 2019 HM Revenue & Customs
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

package navigators.trustees.partnership

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers
import controllers.register.trustees.routes._
import play.api.libs.json.Json

class TrusteesPartnershipDetailsNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesPartnershipDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj())), featureSwitchEnabled = true).build().injector.instanceOf[Navigator]

  "TrusteesPartnershipDetailsNavigator" when {
    "in NormalMode" must {
      def navigationForTrusteePartnership(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(PartnershipDetailsId(index))(partnershipDetails, AddTrusteeController.onPageLoad(mode, None))
        )

      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForTrusteePartnership(NormalMode), None)
    }

    "in UpdateMode" must {
      def navigationForUpdateModeTrusteePartnership(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Expected next page"),
          row(PartnershipDetailsId(index))(partnershipDetails, AddTrusteeController.onPageLoad(mode, srn), Some(newTrusteeUserAnswers))
        )

      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeTrusteePartnership(UpdateMode), srn)
    }
  }

}

object TrusteesPartnershipDetailsNavigatorSpec extends OptionValues {
  private lazy val index = 0
  private val srn = Some("srn")
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val partnershipDetails = PartnershipDetails("test partnership")
}

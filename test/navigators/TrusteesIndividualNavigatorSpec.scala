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

package navigators

import base.SpecBase
import controllers.register.trustees.individual.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models.Mode._
import models.{CheckMode, Mode, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import org.scalatest.prop._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour with Generators {

  import TrusteesIndividualNavigatorSpec._


  val navigator: Navigator = injector.instanceOf[TrusteesIndividualNavigator]

  def normalModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(index))(somePersonNameValue, controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)),
      row(TrusteeDOBId(index))(someDate, controllers.register.trustees.individual.routes.TrusteeHasNINOController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(true, controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(mode, index, None)),
      row(TrusteeHasNINOId(index))(false, controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, controllers.register.trustees.individual.routes.TrusteeHasUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(mode, index, None)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(mode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(mode)),
      row(TrusteeUTRId(index))(someStringValue, cyaIndividualDetailsPage(mode))
    )

  behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalModeRoutes(NormalMode))

  def checkModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeDOBId(index))(someDate, cyaIndividualDetailsPage(CheckMode)),
      row(TrusteeHasNINOId(index))(true, controllers.register.trustees.individual.routes.TrusteeNinoNewController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNewNinoId(index))(someRefValue, cyaIndividualDetailsPage(CheckMode)),
      row(TrusteeHasNINOId(index))(false, controllers.register.trustees.individual.routes.TrusteeNoNINOReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoNINOReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode)),
      row(TrusteeHasUTRId(index))(true, TrusteeUTRController.onPageLoad(CheckMode, index, None)),
      row(TrusteeUTRId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode)),
      row(TrusteeHasUTRId(index))(false, TrusteeNoUTRReasonController.onPageLoad(CheckMode, index, None)),
      row(TrusteeNoUTRReasonId(index))(someStringValue, cyaIndividualDetailsPage(CheckMode))
    )

  behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes(CheckMode))


  def updateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNewNinoId(index))(someRefValue, anyMoreChangesPage)
    )

  behave like navigatorWithRoutesForMode(UpdateMode)(navigator, updateMode(UpdateMode))

}

object TrusteesIndividualNavigatorSpec {
  private val index = 0 // intsAboveValue(-1).sample.value
  private val someDate =  LocalDate.now() // arbitrary[LocalDate].sample.value

  private def cyaIndividualDetailsPage(mode: Mode): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
}

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
import config.{FeatureSwitchManagementServiceTestImpl, FrontendAppConfig}
import connectors.FakeUserAnswersCacheConnector
import identifiers.{IsBeforeYouStartCompleteId, UserResearchDetailsId}
import identifiers.register._
import models._
import models.address.Address
import models.register.{SchemeDetails, SchemeType}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class RegisterNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  override lazy val frontendAppConfig = frontendAppConfigWithHubEnabled

  private def routesWithRestrictedEstablisher = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    // Start - continue or what you will need
    (ContinueRegistrationId, emptyAnswers, beforeYouStart, false, None, false),
    (ContinueRegistrationId, beforeYouStartInProgress, beforeYouStart, false, None, false),
    (ContinueRegistrationId, beforeYouStartCompleted, taskList, false, None, false),

    // Scheme registration
    (SchemeDetailsId, emptyAnswers, schemeEstablishedCountry(NormalMode), true, Some(checkYourAnswers), true),
    (SchemeEstablishedCountryId, emptyAnswers, membership(NormalMode), true, Some(checkYourAnswers), true),
    (MembershipId, emptyAnswers, membershipFuture(NormalMode), true, Some(checkYourAnswers), true),
    (MembershipFutureId, emptyAnswers, investmentRegulated(NormalMode), true, Some(checkYourAnswers), true),
    (InvestmentRegulatedId, emptyAnswers, occupationalPensionScheme(NormalMode), true, Some(checkYourAnswers), true),
    (OccupationalPensionSchemeId, emptyAnswers, benefits(NormalMode), true, Some(checkYourAnswers), true),
    (BenefitsId, emptyAnswers, securedBenefits(NormalMode), true, Some(checkYourAnswers), true),
    (SecuredBenefitsId, securedBenefitsTrue, benefitsInsurer(NormalMode), true, Some(benefitsInsurer(CheckMode)), true),
    (SecuredBenefitsId, securedBenefitsFalse, uKBankAccount(NormalMode), true, Some(checkYourAnswers), true),
    (SecuredBenefitsId, emptyAnswers, expired, false, Some(expired), false),
    (BenefitsInsurerId, emptyAnswers, insurerPostCodeLookup(NormalMode), true, Some(insurerPostCodeLookup(CheckMode)), true),
    (BenefitsInsurerId, insurerAddress, insurerPostCodeLookup(NormalMode), true, Some(checkYourAnswers), true),
    (InsurerPostCodeLookupId, emptyAnswers, insurerAddressList(NormalMode), true, Some(insurerAddressList(CheckMode)), true),
    (InsurerAddressListId, emptyAnswers, insurerAddress(NormalMode), true, Some(insurerAddress(CheckMode)), true),
    (InsurerAddressId, emptyAnswers, uKBankAccount(NormalMode), true, Some(checkYourAnswers), true),
    (UKBankAccountId, ukBankAccountTrue, uKBankDetails(NormalMode), true, Some(uKBankDetails(CheckMode)), true),
    (UKBankAccountId, ukBankAccountFalse, checkYourAnswers, true, Some(checkYourAnswers), true),
    (UKBankAccountId, emptyAnswers, expired, false, Some(expired), false),
    (UKBankDetailsId, emptyAnswers, checkYourAnswers, true, Some(checkYourAnswers), true),
    (CheckYourAnswersId, emptyAnswers, taskList, true, None, false),

    // Review, declarations, success - return from establishers
    (SchemeReviewId, hasCompanies, declarationDormant, true, None, false),
    (SchemeReviewId, hasPartnership, declarationDormant, true, None, false),
    (SchemeReviewId, emptyAnswers, declaration, true, None, false),
    (DeclarationDormantId, emptyAnswers, declaration, true, None, false),
    (DeclarationId, hasEstablishers, schemeSuccess, false, None, false),
    (DeclarationDutiesId, dutiesTrue, adviserCheckYourAnswers, true, Some(adviserCheckYourAnswers), true),
    (DeclarationDutiesId, dutiesFalse, adviserName, true, Some(adviserName), true),
    (DeclarationDutiesId, emptyAnswers, expired, false, None, false),

    // User Research page - return to SchemeOverview
    (UserResearchDetailsId, emptyAnswers, schemeOverview(frontendAppConfig), false, None, false)
  )

  private val config = app.injector.instanceOf[Configuration]
  val fakeFeatureSwitchManagementService = new FeatureSwitchManagementServiceTestImpl(config, environment)


  "RegisterNavigator" must {
    fakeFeatureSwitchManagementService.change("enable-hub-v2", true)
    val navigator = new RegisterNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, fakeFeatureSwitchManagementService)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routesWithRestrictedEstablisher, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec extends OptionValues{

  private val lastPage: Call = Call("GET", "http://www.test.com")

  private val emptyAnswers = UserAnswers(Json.obj())

  private val securedBenefitsTrue = UserAnswers().securedBenefits(true)
  private val securedBenefitsFalse = UserAnswers().securedBenefits(false)
  private val ukBankAccountTrue = UserAnswers().ukBankAccount(true)
  private val ukBankAccountFalse = UserAnswers().ukBankAccount(false)
  private val dutiesTrue = UserAnswers().declarationDuties(true)
  private val dutiesFalse = UserAnswers().declarationDuties(false)
  private val hasCompanies = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test-company-name", None, None))
  private val hasPartnership = UserAnswers().establisherPartnershipDetails(0, models.PartnershipDetails("test-company-name"))
  private val hasEstablishers = hasCompanies.schemeDetails(SchemeDetails("test-scheme-name", SchemeType.GroupLifeDeath))
  private val savedLastPage = UserAnswers().lastPage(LastPage(lastPage.method, lastPage.url))
  private val insurerAddress = UserAnswers().insurerAddress(Address("line-1", "line-2", None, None, None, "GB"))
  private val beforeYouStartCompleted = UserAnswers().set(IsBeforeYouStartCompleteId)(true).asOpt.value
  private val beforeYouStartInProgress = UserAnswers().set(IsBeforeYouStartCompleteId)(false).asOpt.value

  private def benefits(mode: Mode) = controllers.register.routes.BenefitsController.onPageLoad(mode)

  private def benefitsInsurer(mode: Mode) = controllers.register.routes.BenefitsInsurerController.onPageLoad(mode)

  private def checkYourAnswers = controllers.register.routes.CheckYourAnswersController.onPageLoad()

  private def declaration = controllers.register.routes.DeclarationController.onPageLoad()

  private def declarationDormant = controllers.register.routes.DeclarationDormantController.onPageLoad()

  private def insurerAddress(mode: Mode) = controllers.register.routes.InsurerAddressController.onPageLoad(mode)

  private def insurerAddressList(mode: Mode) = controllers.register.routes.InsurerAddressListController.onPageLoad(mode)

  private def insurerPostCodeLookup(mode: Mode) = controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(mode)

  private def investmentRegulated(mode: Mode) = controllers.register.routes.InvestmentRegulatedController.onPageLoad(mode)

  private def membershipFuture(mode: Mode) = controllers.register.routes.MembershipFutureController.onPageLoad(mode)

  private def membership(mode: Mode) = controllers.register.routes.MembershipController.onPageLoad(mode)

  private def occupationalPensionScheme(mode: Mode) = controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(mode)

  private def schemeEstablishedCountry(mode: Mode) = controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(mode)

  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def securedBenefits(mode: Mode) = controllers.register.routes.SecuredBenefitsController.onPageLoad(mode)

  private def uKBankAccount(mode: Mode) = controllers.register.routes.UKBankAccountController.onPageLoad(mode)

  private def uKBankDetails(mode: Mode) = controllers.register.routes.UKBankDetailsController.onPageLoad(mode)

  private def whatYouWillNeed = controllers.routes.WhatYouWillNeedController.onPageLoad()

  private def beforeYouStart = controllers.routes.BeforeYouStartController.onPageLoad()

  private def adviserName = controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def schemeOverview(appConfig: FrontendAppConfig) = appConfig.managePensionsSchemeOverviewUrl

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad()

  private def adviserCheckYourAnswers: Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()


}

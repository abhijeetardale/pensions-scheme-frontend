/*
 * Copyright 2020 HM Revenue & Customs
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

package utils.hstasklisthelper

import helpers.DataCompletionHelper
import identifiers._
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import models.person.PersonName
import models.register.SchemeType
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import utils.{Enumerable, UserAnswers}
import viewmodels.{Message, SchemeDetailsTaskList, SchemeDetailsTaskListEntitySection}

class HsTaskListHelperRegistrationSpec extends WordSpec with MustMatchers with MockitoSugar with DataCompletionHelper {

  import HsTaskListHelperRegistrationSpec._

  private val mockAllSpokes = mock[AllSpokes]
  private val helper = new HsTaskListHelperRegistration(mockAllSpokes)

  "h1" must {
    "display appropriate heading" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.schemeName(name)

      helper.taskList(userAnswers, None, None).h1 mustBe name
    }
  }

  "beforeYouStartSection " must {
    "return correct the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      val expectedBeforeYouStartSection = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader)

      helper.beforeYouStartSection(userAnswers) mustBe expectedBeforeYouStartSection
    }
  }

  "aboutSection " must {
    "return the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAboutSection = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader)
      when(mockAllSpokes.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)

      helper.aboutSection(userAnswers, NormalMode, None) mustBe expectedAboutSection
    }
  }

  "workingKnowledgeSection " must {

    "be empty when do you have working knowledge is true " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value

      helper.workingKnowledgeSection(userAnswers) mustBe None
    }

    "have correct entity section when do you have working knowledge is false " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(false).asOpt.value
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(wkAddLinkText, wkWynPage), None))
      val expectedWkSection = SchemeDetailsTaskListEntitySection(None, expectedSpoke, None)
      when(mockAllSpokes.getWorkingKnowledgeSpoke(any(), any(), any(), any(), any())).thenReturn(expectedSpoke)

      helper.workingKnowledgeSection(userAnswers).value mustBe expectedWkSection
    }
  }

  "addEstablisherHeader " must {
    "have a link to establishers kind page when no establishers are added " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
          controllers.register.establishers.routes.EstablisherKindController
            .onPageLoad(NormalMode, userAnswers.allEstablishers(NormalMode).size, None).url))), None)

      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe expectedAddEstablisherHeader
    }

    "have a link to add establishers page when establishers are added" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(
        None, Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_change_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url))), None)

      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe expectedAddEstablisherHeader
    }
  }

  "addTrusteeHeader " must {
    "have change link to go to add trustees page when trustees are not mandatory(have any trustees queation not asked)" +
      "but there are one or more trustees " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url))), None)

      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have change link to go to add trustees page when trustees is mandatory(have any trustees is true) and there are one or more trustees " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url))), None)

      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have add link to go to trustee kind page when when trustees are not mandatory(have any trustees question not asked)" +
      " and there are no trustees " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url))), None)

      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have add link to go to trustee kind page when trustees is mandatory(have any trustees is true) and there are no trustees " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(value = true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value

      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url))), None)

      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "not be displayed when do you have any trustees is false " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(false).asOpt.value

      helper.addTrusteeHeader(userAnswers, NormalMode, None) mustBe None
    }
  }

  "establishersSection" must {
    "return the seq of establishers without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0).
        establisherCompanyEntity(index = 1, isDeleted = true).
        establisherIndividualEntity(index = 2).
        establisherIndividualEntity(index = 3, isDeleted = true).
        establisherPartnershipEntity(index = 4, isDeleted = true).
        establisherPartnershipEntity(index = 5)

      when(mockAllSpokes.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getEstablisherIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.establishersSection(userAnswers, NormalMode, None)

      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 2 last 2")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 5")))
    }
  }

  "trusteesSection" must {
    "return the seq of trustees without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.trusteeCompanyEntity(index = 0, isDeleted = true).
        trusteeCompanyEntity(index = 1).
        trusteeIndividualEntity(index = 2, isDeleted = true).
        trusteeIndividualEntity(index = 3).
        trusteePartnershipEntity(index = 4).
        trusteePartnershipEntity(index = 5, isDeleted = true)

      when(mockAllSpokes.getTrusteeCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getTrusteeIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getTrusteePartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.trusteesSection(userAnswers, NormalMode, None)

      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 1")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 3 last 3")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 4")))
    }
  }

  "declaration section" must {

    "have a link when declaration is enabled with trustees completed" in {
      val userAnswers = answersDataAllComplete()

      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }

    "have link when all the seqEstablishers are completed without trustees and do you have trustees is false " in {
      val userAnswers = setCompleteBeforeYouStart(isComplete = true,
        setCompleteMembers(isComplete = true,
          setCompleteBank(isComplete = true,
            setCompleteBenefits(isComplete = true,
              setCompleteEstIndividual(index = 0, setCompleteWorkingKnowledge(isComplete = true, userAnswersWithSchemeName)))))).
        haveAnyTrustees(haveTrustees = false)

      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }

    "not have link when about bank details section not completed" in {
      val userAnswers = answersDataAllComplete(isCompleteAboutBank = false)

      helper.declarationSection(userAnswers).value mustBe SchemeDetailsTaskListEntitySection(None, Nil,
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete"
      )
    }
  }

  "task list" must {
    "return the task list with all the sections" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0)
        .set(HaveAnyTrusteesId)(false).asOpt.value
        .set(DeclarationDutiesId)(value = true).asOpt.value

      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      when(mockAllSpokes.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
      when(mockAllSpokes.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)

      val result = helper.taskList(userAnswers, None, None)

      result mustBe SchemeDetailsTaskList(
        schemeName, None,
        beforeYouStart = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader),
        about = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader),
        workingKnowledge = None,
        addEstablisherHeader = Some(SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__sectionEstablishers_change_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url))), None)
        ),
        establishers = Seq(
          SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0"))
        ),
        addTrusteeHeader = None,
        trustees = Nil,
        declaration = Some(
          SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
            "messages__schemeTaskList__sectionDeclaration_incomplete")
        )
      )
    }
  }
}

object HsTaskListHelperRegistrationSpec extends DataCompletionHelper with Enumerable.Implicits {

  private val schemeName = "scheme"
  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  private val beforeYouStartLinkText = Message("messages__schemeTaskList__before_you_start_link_text", schemeName)
  private val beforeYouStartHeader = Some(Message("messages__schemeTaskList__before_you_start_header"))
  private val aboutHeader = Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
  private val whatYouWillNeedMemberPage = controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url
  private val addMembersLinkText = Message("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private val wkAddLinkText = Message("messages__schemeTaskList__add_details_wk")
  private val wkWynPage = controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url

  private val expectedBeforeYouStartSpoke = Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
    controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), Some(false)))

  private val expectedAboutSpoke = Seq(EntitySpoke(TaskListLink(addMembersLinkText, whatYouWillNeedMemberPage), None))
  private val testCompanyEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test company link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))
  private val testIndividualEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test individual link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))
  private val testPartnershipEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))

  private val declarationSectionWithLink = SchemeDetailsTaskListEntitySection(None,
    Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__declaration_link"),
      controllers.register.routes.DeclarationController.onPageLoad().url))),
    Some("messages__schemeTaskList__sectionDeclaration_header"),
    "messages__schemeTaskList__sectionDeclaration_incomplete"
  )

  private def answersDataAllComplete(isCompleteBeforeStart: Boolean = true,
                                     isCompleteAboutMembers: Boolean = true,
                                     isCompleteAboutBank: Boolean = true,
                                     isCompleteAboutBenefits: Boolean = true,
                                     isCompleteWk: Boolean = true,
                                     isCompleteEstablishers: Boolean = true,
                                     isCompleteTrustees: Boolean = true,
                                     isChangedInsuranceDetails: Boolean = true,
                                     isChangedEstablishersTrustees: Boolean = true
                                    ): UserAnswers = {
    setCompleteBeforeYouStart(isCompleteBeforeStart,
      setCompleteMembers(isCompleteAboutMembers,
        setCompleteBank(isCompleteAboutBank,
          setCompleteBenefits(isCompleteAboutBenefits,
            setCompleteEstIndividual(0,
              setCompleteTrusteeIndividual(0,
                setCompleteWorkingKnowledge(isCompleteWk, userAnswersWithSchemeName)))))))
  }
}

package com.redhat.lightblue.facade.loginc

import org.junit.Test
import org.junit.Assert._
import com.redhat.lightblue.facade.inclog.IncLogEntryExtractor.IncLogDiffExtractor
import com.redhat.lightblue.facade.inclog.IncLogCountDiff
import com.redhat.lightblue.facade.inclog.IncLogPathDiff
import com.redhat.lightblue.facade.inclog.IncLogPathDiff

class IncLogDiffExtractorTest {

    @Test
    def test1 {

        "[]: Expected 111 values but got 122" match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogCountDiff])
            }
            case _ => fail()
        }

    }

    @Test
    def test2 {

        "[active=true].personalInfo.emailConfirmed,Expected: true," match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogPathDiff])

                assertEquals("personalInfo.emailConfirmed", x.asInstanceOf[IncLogPathDiff].fields(0).path)
                assertEquals(1, x.asInstanceOf[IncLogPathDiff].fields.length)
            }
            case _ => fail()
        }
    }

    @Test
    def test3 {

        "[createdByUserName=SYSADMIN].subscriptionProducts[]: Expected 3 values but got 5" match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogPathDiff])

                assertEquals("subscriptionProducts", x.asInstanceOf[IncLogPathDiff].fields(0).path)
                assertEquals(1, x.asInstanceOf[IncLogPathDiff].fields.length)
            }
            case _ => fail()
        }
    }

    @Test
    def test4 {

        "[createdDate=1360342213000].subscriptionProducts[]: Expected 4 values but got 2 ; [createdDate=1445544513000].subscriptionProducts[]: Expected 2 values but got 1" match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogPathDiff])

                assertEquals("subscriptionProducts", x.asInstanceOf[IncLogPathDiff].fields(0).path)
                assertEquals("subscriptionProducts", x.asInstanceOf[IncLogPathDiff].fields(1).path)
                assertEquals(2, x.asInstanceOf[IncLogPathDiff].fields.length)
            }
            case _ => fail()
        }
    }

    @Test
    def test5 {

        "[internalLastUpdatedDate=1462889346000],Expected: a JSON object,     but none found, ; [internalLastUpdatedDate=1429927018000],Expected: a JSON object,     but none found, ; [internalLastUpdatedDate=1377177802000],Unexpected: a JSON object, ; [internalLastUpdatedDate=1377177791000],Unexpected: a JSON object," match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogPathDiff])

                assertEquals("<this>", x.asInstanceOf[IncLogPathDiff].fields(0).path)
                assertEquals("<this>", x.asInstanceOf[IncLogPathDiff].fields(1).path)
                assertEquals("<this>", x.asInstanceOf[IncLogPathDiff].fields(2).path)
                assertEquals("<this>", x.asInstanceOf[IncLogPathDiff].fields(3).path)
                assertEquals(4, x.asInstanceOf[IncLogPathDiff].fields.length)
            }
            case _ => fail()
        }
    }

    @Test
    def test6 {

        "" match {
            case IncLogDiffExtractor(x) => {
                assertTrue(x.isInstanceOf[IncLogPathDiff])

                assertEquals("<empty>", x.asInstanceOf[IncLogPathDiff].fields(0).path)
                assertEquals(1, x.asInstanceOf[IncLogPathDiff].fields.length)
            }
            case _ => fail()
        }
    }

}
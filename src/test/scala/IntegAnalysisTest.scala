/**
  * Created by AhmedMetwally on 11/12/16.
  */

import java.nio.file.{Files, Paths}
import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.junit.{Assert, Test}
import sys.process._

class IntegAnalysisTest extends TestKit(ActorSystem("TestActor")) {
  val actorRef = TestActorRef[AnalyzerActor.ComparisonActor]
  val actor = actorRef.underlyingActor

  @Test
  def testAnalysisIntegration {
    val x = actor.createUDBFiles("datasets/square")
    actor.createDepGraphs(Array("okio", "okio_v2"))
    actor.analyzeResults("datasets/square")

    println(actor.num_added_nodes)
    println(actor.num_deleted_nodes)
    assert(actor.num_added_nodes == 184)
    assert(actor.num_deleted_nodes == 11)
    println("Passed...")
  }

}
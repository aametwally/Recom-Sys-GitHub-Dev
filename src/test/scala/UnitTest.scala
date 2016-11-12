import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.junit.{Assert, Test}
import sys.process._
/**
  * Created by samrudhinayak on 11/11/16.
  */
class UnitTest extends TestKit(ActorSystem("TestActor")) {
 val actorRef = TestActorRef[AnalyzerActor.ComparisonActor]
  val actor = actorRef.underlyingActor


  @Test
  def testUDB {

    val projectPath : String = "datasets/square"
    val list_of_version_subfolders = actor.createUDBFiles(projectPath)

    assert(Files.exists(Paths.get("okio.udb")))
    assert(Files.exists(Paths.get("okio_v2.udb")))
    println("Passed...")
  }

  @Test
  def testCreateDep {
    actor.createDepGraphs(Array("okio", "okio_v2"))

    println(actor.dg_objs(0).vertexSet().size())
    println(actor.dg_objs(1).vertexSet().size())
    println(actor.dg_objs(0).edgeSet().size())
    println(actor.dg_objs(1).edgeSet().size())

    assert(actor.dg_objs(0).vertexSet().size() == 1059)
    assert(actor.dg_objs(1).vertexSet().size() == 886)
    assert(actor.dg_objs(0).edgeSet().size() == 3296)
    assert(actor.dg_objs(1).edgeSet().size() == 2617)
    println("Passed...")
  }

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

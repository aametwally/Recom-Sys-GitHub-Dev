import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.junit.{Assert, Test}

/**
  * Created by samrudhinayak on 11/11/16.
  */
class UnitTest extends TestKit(ActorSystem("TestActor")) {

 val actorRef = TestActorRef[AnalyzerActor.ComparisonActor]
  val actor = actorRef.underlyingActor
  @Test
  def test1 {
    val sam = "0"
    val y = "0"
    println(actor.retstring())
    println(actor.retdelnodes())
    Assert.assertEquals(actor.retstring(),sam)
    Assert.assertEquals(actor.retdelnodes(),y)
  }
}

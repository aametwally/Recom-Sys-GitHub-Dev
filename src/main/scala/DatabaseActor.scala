import akka.actor.Actor
import akka.stream.ActorMaterializer

/**
  * Created by samrudhinayak on 11/8/16.
  */
object DatabaseActor extends App {

  class ComparisonActor extends Actor {
    implicit val materializer = ActorMaterializer()
    def receive= {
      case repoString: String => {
    println("Repo string: " +repoString)
    }
    }

  }

}

package data

import akka.actor._

object RequestsSocketActor {
  def props(out: ActorRef) = Props(classOf[RequestsSocketActor], out)
  object SendRequests
}

class RequestsSocketActor(out: ActorRef) extends Actor with ActorLogging {


  def receive = {
    case msg: String => {

    }
    case RequestsSocketActor.SendRequests =>{

    }
  }
}

package jug.workshops.reactive.akka.basics.answers

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import common.StopSystemAfterAll
import jug.workshops.reactive.akka.basics.answers.PingPongProtocol.{Ball, StartGame}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by pawel on 30.10.16.
  */
class BasicsPart6TestingActorAnswer extends TestKit(ActorSystem("test")) with MustMatchers
    with WordSpecLike with StopSystemAfterAll with ImplicitSender{

  "ActorA" should {
    "start playing when 'Start' is sent " in {
      val probe=TestProbe()
      val actorA=system.actorOf(Props(new ActorA(probe.ref,1)))

      actorA ! StartGame

      probe.expectMsg(Ball(1))
    }

    "play for n rounds" in {
      val probe=TestProbe()
      val actorA=system.actorOf(Props(new ActorA(probe.ref,3)))

      actorA ! StartGame
      actorA ! Ball(1)
      actorA ! Ball(2)
      actorA ! Ball(3)

      probe.expectMsg(Ball(1))
      probe.expectMsg(Ball(2))
      probe.expectMsg(Ball(3))

      import scala.concurrent.duration._

      probe.expectNoMsg(500 millis)
    }

    "ActorB" should {
      "play for n rounds" in {
        val actorB=system.actorOf(Props(new ActorB(3)))

        actorB ! StartGame
        actorB ! Ball(1)
        actorB ! Ball(2)
        actorB ! Ball(3)

        expectMsg(Ball(1))
        expectMsg(Ball(2))
        expectMsg(Ball(3))

        import scala.concurrent.duration._

        expectNoMsg(500 millis)
      }
    }
  }


}


object PingPongProtocol{
  case object StartGame
  case class Ball(round:Int=1)
}

class ActorA(player:ActorRef, limit:Int) extends Actor with ActorLogging{
  override def receive: Receive = {
    case StartGame =>  player ! Ball()
    case Ball(round) if round<limit => player ! Ball(round+1)
    case _ : Ball =>
      log.info("ActorA stop playing")
      self ! PoisonPill
  }
}

class ActorB(limit:Int) extends Actor with ActorLogging{

  var roundsRemain=limit

  override def receive: Receive = {
    case b:Ball if(roundsRemain>0)=> sender ! b
    case _:Ball =>
      log.info("ActorB stop playing")
      self ! PoisonPill
  }
}

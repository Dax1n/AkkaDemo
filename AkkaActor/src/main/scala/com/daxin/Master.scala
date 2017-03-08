package com.daxin


import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._


class Master(val host: String, val port: Int) extends Actor {

  //超时检查的间隔
  val CHECK_INTERVAL = 15000

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {

    println("preStart ....")

    /**
      * Returns the dispatcher (MessageDispatcher) that is used for this Actor.
      * Importing this member will place an implicit ExecutionContext in scope.
      */
    import context.dispatcher

    //初始延迟，间隔，接受者，消息
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)

  }


  //必须返回一个偏函数
  override def receive: Receive = {


    case RegisterWorker(id,cores,memoty)=>{
      println("id = "+id+" worker 注册完毕！")
      //sender该次消息的发送者，此处就是worker
      sender ! RegisteredWorker(id)
    }

    case Heart(workerId)=>{
      println("收到id为"+workerId+"的心跳~！")
    }

    case CheckTimeOutWorker=>{
      println("Master 进行worker超时检查...")

    }
  }
}


object Master {


  def main(args: Array[String]) {

    val host = "192.168.1.102"
    val port = 8080
    // 准备配置
    val configStr =

    //RemoteActorRefProvider: Starts up actor on remote node and creates a RemoteActorRef representing it.
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("MasterSystem", config)
    //创建Actor
    val master = actorSystem.actorOf(Props(new Master(host, port)), "Master")
    actorSystem.awaitTermination()
  }

}

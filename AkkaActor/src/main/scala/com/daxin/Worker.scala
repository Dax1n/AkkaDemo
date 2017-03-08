package com.daxin

import java.util.UUID

import scala.concurrent.duration._
import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by Daxin on 2017/3/8.
  */
class Worker (val host:String,val port:Int,val cores:Int,val memory:Int) extends  Actor{

  /**
    * An ActorSelection is a logical view of a section of an ActorSystem's tree of Actors,
    * allowing for broadcasting of messages to that section.
    */
  var master : ActorSelection = _

  val workerId = UUID.randomUUID().toString

  val HEART_INTERVAL = 1000

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {

    //跟Master建立连接,MasterSystem为ActorSystem的名字，Master为Master的ActorRef的名字
    master = context.actorSelection(s"akka.tcp://MasterSystem@$host:$port/user/Master")//获取到Master的ActorRef


    //向Master发送注册消息
    master ! RegisterWorker(workerId, memory, cores)
  }
  override def receive: Receive = {
    case RegisteredWorker(id) =>{

      println("id = "+id+"，注册完毕！")
      import context.dispatcher

      context.system.scheduler.schedule(0 millis,HEART_INTERVAL millis,self,Heart(workerId))
    }

    case Heart(workerId) =>{

      println("Worker 向Master发送心跳~！")
      master!Heart(workerId)
    }

  }
}


object Worker{

  def main(args: Array[String]): Unit = {
    val workerHost = "192.168.1.102"
    val workerPort = 5566
    val masterHost ="192.168.1.102"
    val masterPort =8080
    val memory = 4
    val cores = 16
    // 准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$workerHost"
         |akka.remote.netty.tcp.port = "$workerPort"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，辅助创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("WorkerSystem", config)
    val workerRef=actorSystem.actorOf(Props(new Worker(masterHost, masterPort, memory, cores)), "Worker")

    actorSystem.awaitTermination()



  }

}

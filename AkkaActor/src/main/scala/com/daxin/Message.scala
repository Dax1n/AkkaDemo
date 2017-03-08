package com.daxin

/**
  * Created by Daxin on 2017/3/8.
  */
trait Message extends Serializable
case object CheckTimeOutWorker extends Message
case class RegisterWorker(id:String,cores:Int,memory:Int) extends Message
case class RegisteredWorker(id:String) extends Message
case class Heart(workerId:String) extends Message
package com.company.main

import com.company.main.services.BidService
import aksharp.bidder.BidderGrpc

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object Main extends App {

  implicit val ec: ExecutionContext = global

  val bidService: BidderGrpc.Bidder = new BidService

  aksharp.bidder.server.run(
    bidder = bidService
  )

}

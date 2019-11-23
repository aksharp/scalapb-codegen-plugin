package com.company.main

import com.company.main.services.BidService
import io.nomadic.bidder.BidderGrpc

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object Main extends App {

  implicit val ec: ExecutionContext = global

  val bidService: BidderGrpc.Bidder = new BidService

  io.nomadic.bidder.server.run(
    bidder = bidService
  )

}

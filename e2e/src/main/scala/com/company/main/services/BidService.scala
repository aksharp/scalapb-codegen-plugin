package com.company.main.services

import aksharp.bidder.{BidRequest, BidResponse, BidderGrpc}

import scala.concurrent.Future

class BidService extends BidderGrpc.Bidder {

  val defaultPlacement = 0

  override def bid(request: BidRequest): Future[BidResponse] = {
    Future.successful(
      BidResponse(
        placementId = request.placements.map(_.id).headOption.getOrElse(defaultPlacement),
        bidPrice = 9.0
      )
    )
  }

}

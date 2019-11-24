#!/bin/bash -x

# Setup instructions: https://github.com/fullstorydev/grpcurl

echo 'grpcurl -plaintext -d '{"id": 1234}' localhost:8080 aksharp.grpc.Bidder/Bid'
grpcurl -plaintext -d '{"id": 1234}' localhost:8080 aksharp.grpc.Bidder/Bid

# expected result
#{
#  "placementId": "1234",
#  "bidPrice": 45.9
#}

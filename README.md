# Sample Rate Limit app

This is a sample implementation of a rate limited that can be used by a client calling a service which is being rate limited by a server.

This approach creates maxPermist slots - once a slot is in-use, it cannot be released until the configured time duration has passed.



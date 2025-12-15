# observability

this repo contains a spring boot application that attempts to store high cardinality metrics. It uses
two PrometheusMeterRegistrys to separate metrics, one for high cardinality metrics and the other for low.

It also exposes two prometheus endpoints so that can be scraped individually.

This is a proof-of-concept implementation of dependent variables.

Dependent variable is just a variable which value is changed depending on the changes of some other variables.

E.g. we have dependent variable like this:

```scala
val httpClient = depVarContext
    .observeValues(TIMEOUT, MAX_CONN, MAX_TOTAL_CONN)
    .map {
      case List(timeout, maxConn, maxTotalConn) => (timeout.toInt, maxConn.toInt, maxTotalConn.toInt)
    }
    .map {
      case (timeout, maxConn, maxTotalConn) =>
        val httpClient = new HttpClient({
          val connectionMgr = new MultiThreadedHttpConnectionManager()
          connectionMgr.setParams({
            val params = new HttpConnectionManagerParams()
            import params._
            setSoTimeout(timeout * 1000)
            setConnectionTimeout(timeout * 1000)
            setDefaultMaxConnectionsPerHost(maxConn)
            setMaxTotalConnections(maxTotalConn)
            params
          })
          connectionMgr
        })
        httpClient.getParams.setAuthenticationPreemptive(true)
        httpClient
    }
```

We can use it this way:

```scala
httpClient().executeMethod(getMethod);
```

The value of this variable will not be reinitialized till any of primary variables, such as`timeout`, `maxConn` or 
`maxTotalConn` is changed. As soon as any if primary variable is change, a dependent variable will be changed as well.

This change is transparent for the client code, so we can use same `depVar()` or `depVar.apply()`.

What I want to obtain is monadic dependent var context:

```scala
for {
  x <- observe("x", defaultX) // x value or defaultX
  y <- observe("y")           // option on y
} yield someExpressionDependingOn(x, y)
```

# Deadman Switch

A deadman switch as a service.

## Motivations

The motivation for writing this service came from the question (paraphrased):

I know how to make sure something does happen, but how to I ensure that action is taken when something DOESN'T happen?

## Use Cases

- Mortgage compliance violation reporting
- Batch job execution monitoring (eg data warehouse loads)
- Complementary functions (report whether function, _g_ is executed within a certain amount of time after function _f_ executes)
- Online training deadlines
- ...

## Definitions

**Task**

A task represents some unit of work that must be completed before a specific deadline. If the task is not completed before the
deadline, the task expires; causing expiration records to be written to a C* keyspace.

**Aggregate**

A high level grouping of tasks. An aggregate should contain no more than 1,000 tasks.

**Entity**

A secondary grouping of tasks within an aggregate. An aggregate can contain one or more entity groupings.

**Key**

A human readable identifier for a single task. The key must be unique within an entity grouping. A key must be a non-empty string
consisting of upper-case chars, lower-case chars and numbers.

**Tag**

A series of custom task identifiers. These can be used to arbitrarily query expired tasks within a time window (day, week, or month).
Tags must be lower-case strings WITHOUT spaces, numbers or special characters.

## Project Layout

**domain**

Contains all command, event and query domain objects. Domain objects are represented as protocol buffers that are compiled to Scala
case classes. Protocol buffers are a better alternative than Java serialization for event persistence. Also handles domain validation
using cats `Validated`.

**core**

Contains all `Eventuate` event-sourcing components, event persistence and replication.

**service**

Contains the HTTP/JSON serving layer. Uses Akka streams to control the rate at which commands are sent to the core system.

**client**

TODO: Scala client lib

**client-java**

TODO: Java client lib

**load**

Contains manual integration testing logic. Can load a large amount of tasks into the service. Once loaded, tasks can be completed or,
after waiting for task expiration, validated. Validation confirms that all expired tasks are written to the C* keyspace.

## TODO

- Java client lib
- Scala client lib
- User Interface: Create a Play Scala web application for querying expirations and warnings
- Java annotations lib: Add the ability to annotate java methods with @Schedule and @Complete (nice-to-have)
- Play Scala lib: Add actions for scheduling and completing tasks (optional)

## References

- [Akka](https://akka.io/)
- [Cassandra](https://cassandra.apache.org/)
- [Cats](https://typelevel.github.io/cats/)
- [Eventuate](https://rbmhtechnology.github.io/eventuate/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers/)
- [Quill](http://getquill.io/)
- [ScalaPB](https://scalapb.github.io/)

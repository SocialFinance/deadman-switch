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

## TODO

- Java annotations lib: Add the ablity to annotate java methods with @Schedule and @Complete
- User Interface: Create a Play Scala web applicatoin for querying expirations and warnings
- Play Scala lib: Add actions for scheduling and completing tasks (optional)

## References

- [Akka](https://akka.io/)
- [Cassandra](https://cassandra.apache.org/)
- [Cats](https://typelevel.github.io/cats/)
- [Eventuate](https://rbmhtechnology.github.io/eventuate/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers/)
- [Quill](http://getquill.io/)
- [ScalaPB](https://scalapb.github.io/)

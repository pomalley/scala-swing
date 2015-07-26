package wh

/**
 * A collection of models into a unit.
 */
class Squad(val modelType: ModelType, val name: String) {
  var models: List[Model] = List()

  def matches(other: ModelType): Boolean = modelType == other
  def matches(other: Model): Boolean = matches(other.modelType)
  def matches(other: Squad): Boolean = matches(other.modelType)

  def contains(model: Model): Boolean = models.contains(model)

  def add(model: Model): Unit = {
    require(matches(model))
    models ::= model
  }
}

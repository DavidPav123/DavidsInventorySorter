package commands.datastructures

class GraphNode<T>(@JvmField var value: T) {
    val children: MutableList<GraphNode<T>>
    private val parents: MutableList<GraphNode<T>>

    init {
        children = ArrayList()
        parents = ArrayList()
    }

    private fun addParent(node: GraphNode<T>) {
        parents.add(node)
    }

    fun addChild(node: GraphNode<T>) {
        children.add(node)
        node.addParent(this)
    }

    fun hasChild(): Boolean {
        return children.size > 0
    }

    fun hasParent(): Boolean {
        return parents.size > 0
    }

    fun getParents(): List<GraphNode<T>> {
        return parents
    }

    override fun toString(): String {
        return "Node:" + value.toString()
    }
}

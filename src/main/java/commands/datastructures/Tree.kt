package commands.datastructures

import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

open class Tree<T>(rootValue: T) : Iterable<GraphNode<T>?> {
    /**
     * Returns the root of the tree.
     *
     * @return the root of the tree.
     */
    @JvmField
    val root: GraphNode<T>

    init {
        root = GraphNode(rootValue)
    }

    fun getNodeFormChildren(parent: GraphNode<T>, predicate: Predicate<T>): GraphNode<T>? {
        for (child in parent.children) {
            if (predicate.test(child.value)) {
                return child
            }
        }
        return null
    }

    private fun toList(node: GraphNode<T>): MutableList<GraphNode<T>> {
        var list: MutableList<GraphNode<T>> = ArrayList()
        list.add(node)
        for (child in node.children) {
            val childList: List<GraphNode<T>> = toList(child)
            list = Stream.concat(list.stream(), childList.stream()).collect(Collectors.toList())
        }
        return list
    }

    override fun toString(): String {
        return buildStringForNode(this.root, "")
    }

    private fun buildStringForNode(node: GraphNode<T>, tabs: String): String {
        val builder = StringBuilder()
        builder.append(tabs).append("├ ").append(node)
        val hasChild = node.hasChild()
        if (hasChild) builder.append("\n")
        for (n in node.children) {
            builder.append(buildStringForNode(n, "$tabs|\t")).append("\n")
        }
        if (hasChild) builder.deleteCharAt(builder.length - 1)
        return builder.toString()
    }

    /**
     * Returns an iterator over elements of type `T`.
     *
     * @return an Iterator.
     */
    override fun iterator(): MutableIterator<GraphNode<T>> {
        return toList(this.root).iterator()
    }
}

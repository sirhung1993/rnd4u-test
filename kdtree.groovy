class KdTree {
    KdNode root

    class KdNode {
        int from
        int to
        String val
        KdNode next
    }

    KdNode findAndInsert(KdNode currentNode, KdNode newNode) {
        if (currentNode.from > newNode.from ) {
            
        } else {

        }
    }

    void insert(KdNode node) {
        if (root != null) {
            
        } else {
            root = node
        }
    }
}
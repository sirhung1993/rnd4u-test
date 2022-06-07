import groovy.transform.CompileStatic

// ---------------------------
//
// HOMEWORK
//
// You can use either Groovy or Java.
//
// Design a routine that will calculate the average Product price per Group.
//
// The Price of each Product is calculated as:
// Cost * (1 + Margin)
//
// Assume there can be a large number of products and a large number of categories.
//
// Plus points:
// - use Groovy and its closures
// - make the category look-up performance effective
// - use method Collection.inject

// contains information about [Product, Group, Cost]
def products = [
    ['A', 'G1', 20.1],
    ['B', 'G2', 98.4],
    ['C', 'G1', 49.7],
    ['D', 'G3', 35.8],
    ['E', 'G3', 105.5],
    ['F', 'G1', 55.2],
    ['G', 'G1', 12.7],
    ['H', 'G3', 88.6],
    ['I', 'G1', 5.2],
    ['J', 'G2', 72.4]]

// contains information about Category classification based on product Cost
// [Category, Cost range from (inclusive), Cost range to (exclusive)]
// i.e. if a Product has Cost between 0 and 25, it belongs to category C1
def category = [
    ['C3', 50, 75],
    ['C4', 75, 100],
    ['C2', 25, 50],
    ['C5', 100, null],
    ['C1', 0, 25]
    // , ['C6', 1, 25]
    ]

// contains information about margins for each product Category
// [Category, Margin (either percentage or absolute value)]
def margins = [
    'C1' : '20%',
    'C2' : '30%',
    'C3' : '0.4',
    'C4' : '50%',
    'C5' : '0.6']

// ---------------------------
//
// YOUR CODE GOES BELOW THIS LINE
//
// Assign the 'result' variable so the assertion at the end validates
//
// ---------------------------

def cat2Range = [:]
category.each { cat -> cat2Range.put(cat[0], ['from': cat[1], 'to' : cat[2]]) }
cat2Range.each { line -> println(line) }

def convertedMargin = [:]
def percent = '%'
margins.each({ c ->
    double marginVal = c.value.endsWith(percent) ? Double.valueOf(c.value.replace(percent, '')) / 100.0 : Double.valueOf(c.value)
    convertedMargin.put(c.key, marginVal)
});
convertedMargin.each { line -> println(line) }

@CompileStatic
class KdNode {

    int from
    int to
    String val
    KdNode left
    KdNode right

    void setFrom(int from) {
        this.from = from
    }

    void setTo(int to) {
        this.to = to
    }

    void setVal(String val) {
        this.val = val
    }

    @Override
    String toString() {
        return String.format('from : %d , to : %d, val : %s ', from, to, val)
    }

}

@CompileStatic
class KdTree {

    KdNode root

    boolean goLeft(KdNode current, KdNode target) {
        return target.from < current.from
    }

    void findAndInsert(KdNode currentNode, KdNode newNode) {
        if (currentNode == null || newNode == null) {
            println('findAndInsert null')
            return
        }
        boolean left = goLeft(currentNode, newNode)
        if (left) {
            if (currentNode.left != null) {
                findAndInsert(currentNode.left, newNode)
            } else {
                currentNode.left = newNode
            }
        } else {
            if (currentNode.right != null) {
                findAndInsert(currentNode.right, newNode)
            } else {
                currentNode.right = newNode
            }
        }
    }

    void insert(KdNode newNode) {
        if (root != null) {
            findAndInsert(root, newNode)
        } else {
            root = newNode
        }
    }

    void printAllFrom(KdNode currentNode, Closure closure) {
        if (currentNode != null) {
            closure.call(currentNode)
            printAllFrom(currentNode.left, closure)
            printAllFrom(currentNode.right, closure)
        }
    }

    void printAll(Closure closure) {
        printAllFrom(root, closure)
    }

    KdNode findByCost(KdNode currentNode, BigDecimal cost) {
        if (currentNode != null) {
            if (cost < currentNode.from) {
                return findByCost(currentNode.left, cost)
            }
            if (cost < currentNode.to) {
                return currentNode
            }
            return findByCost(currentNode.right, cost)
        }
        return null
    }

    String getCategory(BigDecimal cost) {
        KdNode ret = findByCost(root, cost)
        return ret != null ? ret.val : null
    }

    void findAllByCost(KdNode currentNode, BigDecimal cost, Set<String> outputCategories) {
        if (currentNode != null) {
            if (cost < currentNode.from) {
                findAllByCost(currentNode.left, cost, outputCategories)
            } else {
                if (cost < currentNode.to) {
                    outputCategories.add(currentNode.val)
                }
                findAllByCost(currentNode.left, cost, outputCategories)
                findAllByCost(currentNode.right, cost, outputCategories)
            }
        }
    }

    /*
    * For overlapping interval
    */
    Set<String> getCategories(BigDecimal cost) {
        Set<String> output = []
        findAllByCost(root, cost, output)
        return output
    }

}

// Assume the category is in order already
// fixed interval for category
// Overfit the problem here - will be solved by KDTree later if the interval is not fixed
// def interval = 25
// def categoryInterval = new String[category.size()]
KdTree categoryTree = new KdTree()

// Try to make the tree balancing - ugly way
BigDecimal averageFrom = category.inject(0.0) { sum, ele -> return sum + ele[1] } / category.size()
category.sort{a,b -> 
    BigDecimal diff1 = Math.abs(a[1] - averageFrom)
    BigDecimal diff2 = Math.abs(b[1] - averageFrom)
    return diff1 <=> diff2
}
.each{ c ->
    KdNode kdNode = new KdNode()
    kdNode.val = c[0]
    kdNode.setFrom(c[1])
    int convertTo = c[2] != null ? c[2] : Integer.MAX_VALUE
    kdNode.setTo(convertTo)
    categoryTree.insert(kdNode)
}
// categoryInterval.each({ cat -> println(cat) })
categoryTree.printAll({ node -> println(node.toString()) })

String getCatType(KdTree kdTree, BigDecimal cost) {
    return kdTree.getCategory(cost)
}

Set<String> getCategories(KdTree kdTree, BigDecimal cost) {
    return kdTree.getCategories(cost)
}
Map<String,BigDecimal> result = products.inject([:]) { group2Price, ele ->
    String group = ele[1]
    BigDecimal cost = ele[2]
    String cat = getCatType(categoryTree, cost)
    if (cat == null) {
        println(String.format('Group : %s - Cost %s - Cat %s ', group, cost.toString(), cat))
    }
    //Self-check for over-lapping interval
    Set<String> categoriesTest = getCategories(categoryTree, cost)
    if (categoriesTest.size() > 1) {
        println(String.format('categoriesTest cost %s - size %d', cost.toString(), categoriesTest.size()))
    }

    BigDecimal margin = convertedMargin[cat]
    BigDecimal price = cost * (margin + 1)
    if (group2Price.containsKey(group)) {
        group2Price.get(group).add(price)
    } else {
        group2Price.put(group, [price])
    }
    return group2Price
}.inject([:]) { ret, entry ->
    BigDecimal average = entry.value.inject(0.0) { sum, i -> return sum + i } / entry.value.size();
    ret.put(entry.key, average.round(1))
    return ret
}

[
    'G1' : 37.5,
    'G2' : 124.5,
    'G3' : 116.1
].each({ testEntry ->
        def key = testEntry.key
        def val = testEntry.value
        assert val == result.get(key) : 'Fail here'
})

// ---------------------------
//
// IF YOUR CODE WORKS, YOU SHOULD GET "It works!" WRITTEN IN THE CONSOLE
//
// ---------------------------
assert result == [
    'G1' : 37.5,
    'G2' : 124.5,
    'G3' : 116.1
    ] : "It doesn't work"

println 'It works!'

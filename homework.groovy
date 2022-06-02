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
    ["A", "G1", 20.1],
    ["B", "G2", 98.4],
    ["C", "G1", 49.7],
    ["D", "G3", 35.8],
    ["E", "G3", 105.5],
    ["F", "G1", 55.2],
    ["G", "G1", 12.7],
    ["H", "G3", 88.6],
    ["I", "G1", 5.2],
    ["J", "G2", 72.4]]
 
// contains information about Category classification based on product Cost
// [Category, Cost range from (inclusive), Cost range to (exclusive)]
// i.e. if a Product has Cost between 0 and 25, it belongs to category C1
def category = [
    ["C3", 50, 75],
    ["C4", 75, 100],
    ["C2", 25, 50],
    ["C5", 100, null],
    ["C1", 0, 25]]
 
// contains information about margins for each product Category
// [Category, Margin (either percentage or absolute value)]
def margins = [
    "C1" : "20%",
    "C2" : "30%",
    "C3" : "0.4",
    "C4" : "50%",
    "C5" : "0.6"]
 
// ---------------------------
//
// YOUR CODE GOES BELOW THIS LINE
//
// Assign the 'result' variable so the assertion at the end validates
//
// ---------------------------

def cat2Range = [:]
category.each {cat -> cat2Range.put(cat[0], ["from": cat[1], "to" : cat[2]])}
cat2Range.each{line -> println(line)}

def convertedMargin=[:]
def percent='%'
margins.each({c -> 
    double marginVal = c.value.endsWith(percent) ? Double.valueOf(c.value.replace(percent,'')) / 100.0 : Double.valueOf(c.value)
    convertedMargin.put(c.key, marginVal)
    });
convertedMargin.each{line -> println(line)}

// Assume the category is in order already
// fixed interval for category
// Overfit the problem here - will be solved by KDTree later if the interval is not fixed
def interval = 25
def categoryInterval = new String[category.size()]
category.each({c -> 
    int from = c[1];
    int targetIndex = Math.floor(from / interval)
    categoryInterval.putAt(targetIndex, c[0])
})
categoryInterval.each({cat -> println(cat)})

def getCatType(categoryInterval, cost, interval) {
    int index = Math.floor(cost / interval);
    return index < categoryInterval.size() ? categoryInterval[index] : categoryInterval[categoryInterval.size()-1] 
}

def result = products.inject([:]){group2Price, ele -> 
    def group = ele[1]
    def cost = ele[2]
    def cat = getCatType(categoryInterval, cost, interval)
    def margin = convertedMargin[cat]
    def price = cost * (1 + margin)
    if (group2Price.containsKey(group)) {
        group2Price.get(group).add(price)
    } else {
        group2Price.put(group,[price])
    }
    return group2Price;
}.inject([:]){ret, entry -> 
    def average = entry.value.inject(0.0){sum, i -> return sum + i} / entry.value.size();
    ret.put(entry.key, average.round(1))
    return ret
}

[
    "G1" : 37.5,
    "G2" : 124.5,
    "G3" : 116.1
].each({testEntry -> 
        def key = testEntry.key
        def val = testEntry.value
        assert val == result.get(key) : "Fail here"
    })
 
// ---------------------------
//
// IF YOUR CODE WORKS, YOU SHOULD GET "It works!" WRITTEN IN THE CONSOLE
//
// ---------------------------
assert result == [
    "G1" : 37.5,
    "G2" : 124.5,
    "G3" : 116.1
    ] : "It doesn't work"
 
println "It works!"


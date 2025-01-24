package org

object Main {
  def main(args: Array[String]): Unit = {

    // Define a case class Person with name, age, and address fields. 
    // Case classes are immutable and provide convenient features like pattern matching.
    case class Person(name: String, age: Int, address: String)

    // Define a case class Point with x and y fields. 
    // Case classes are well-suited for representing data with immutable properties.
    case class Point(x: Int, y: Int)

    // Create an instance of Point
    val point1 = Point(1, 2)

    // Create a new instance of Point by copying point1 and incrementing the x value by 1.
    // Demonstrates the use of the copy method for creating new instances with modified fields.
    val point2 = point1.copy(x = point1.x + 1)

    // Define a trait Flyable with a fly method. 
    // Traits define shared behavior or capabilities that can be mixed into classes.
    trait Flyable {
      def fly(): Unit = {
        println("I am flying")
      }
    }

    // Define a trait Shitable with a shit method. 
    // Traits can also have concrete methods providing default behavior.
    trait Shitable {
      def shit(): Unit = {
        println("I am shitting")
      }
    }

    // Define an abstract class Animal that can be extended by concrete classes.
    // Abstract classes cannot be instantiated directly. They provide a blueprint for subclasses.
    abstract class Animal {
      // Abstract method that subclasses must implement.
      def makeSound(): Unit 

      // Optional concrete method inherited by subclasses.
      def breathe(): Unit = {
        println("Breathing...")
      }
    }

    // Define a class Bird that extends Animal, Flyable, and Shitable traits.
    // This demonstrates multiple trait inheritance and inheritance from an abstract class.
    class Bird extends Animal with Flyable with Shitable {
      val hello = "asdasd"

      // Implementation of the abstract makeSound() method from the Animal class.
      override def makeSound(): Unit = {
        println("Tweet!")
      }
    }

    // Create an instance of Bird
    val bird1 = new Bird()

    // Call the fly method on bird1
    bird1.fly()

    // Call the shit method on bird1
    bird1.shit()

    // Call the breathe method on bird1 (inherited from Animal)
    bird1.breathe()

    // Call the makeSound() method on bird1 (implemented by Bird)
    bird1.makeSound()

    // **Differences between Case Classes, Traits, and Abstract Classes:**

    // * **Case Classes:**
    //   - Primarily used for data modeling with immutable fields.
    //   - Provide convenient features like pattern matching and copy method.
    //   - Immutable by default.

    // * **Traits:**
    //   - Define shared behavior or capabilities that can be mixed into classes.
    //   - Can have both abstract and concrete methods.
    //   - Allow for flexible composition of behaviors.

    // * **Abstract Classes:**
    //   - Act as blueprints for concrete classes.
    //   - Can have both abstract and concrete methods.
    //   - Cannot be instantiated directly.
    //   - Provide a more structured approach to inheritance than traits.
  }
}
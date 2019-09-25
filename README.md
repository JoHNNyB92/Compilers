# Calculator,MiniJava compiler

Each section corresponds to the respective folder inside the project.

# Chapter 1: Calculator
The calculator accepts expressions with addition, subtraction, multiplication, and division operators, as well as parentheses. 
It uses the following grammar:
exp  ->   num | exp op exp | (exp)
op    ->   + | - | * | /
num ->   0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

# Chapter 2: Minijava Semantic analysis

MiniJava is designed so that its programs can be compiled by a full Java compiler like javac.

It does not allow global functions, only classes, fields and methods. 
The basic types are int, boolean, and int [] which is an array of int. 
You can build classes that contain fields of these basic types or of other classes.
Classes contain methods with arguments of basic or class types, etc.
MiniJava supports single inheritance but not interfaces. 
It does not support  function overloading, which means that each method name must be unique.
In addition, all methods are inherently polymorphic (i.e., “virtual” in C++ terminology). 
This means that foo can be defined in a subclass if it has the same return type and arguments as in the parent, but it is an error if it exists with other arguments or return type in the parent. Also all methods must have a return type--there are no void methods. 
Fields in the base and derived class are allowed to have the same names, and are essentially different fields.
All MiniJava methods are “public” and all fields “protected”. A class method cannot access fields of another class, with the exception of its superclasses. Methods are visible, however. A class's own methods can be called via “this”. E.g., this.foo(5) calls the object's own foo method, a.foo(5) calls the foo method of object a. Local variables are defined only at the beginning of a method. A name cannot be repeated in local variables (of the same method) and cannot be repeated  in fields (of the same class). A local variable x shadows a field x of the surrounding class.
In MiniJava, constructors and destructors are not defined. 
The new operator  calls a default void constructor. 
In addition, there are no inner classes and there are no static methods or fields. 
By exception, the pseudo-static method “main” is handled specially in the grammar. 
A MiniJava program is a file that begins with a special class that contains the main method and specific arguments that are not used. 
The special class has no fields. After it, other classes are defined that can have fields and methods.

# Chapter 3: Generating Intermediate Code
This part contains visitors that convert MiniJava code into an intermediate language which we are going to call Piglet

# Chapter 4: Generating Intermediate Code
Translation translate the Piglet intermediate representation to an even lower-level IR, which we'll call Spiglet

# Chapter 5: Register allocation
In this stage of the project you will translate the Spiglet code to an even lower level intermediate, Kanga. 
Kanga resembles Spiglet but is closer to the specifics of the MIPS architecture.Register allocation algorithm selected was coloured map.

# Chapter 6: MIPS Code generation
In this project stage you will translate the Kanga code into MIPS assembly code. MIPS registers are exactly those that Kanga uses for its variables

package scala.of.coq

import scala.annotation.tailrec
import scala.of.coq.parsercombinators.lexer.CoqLexer
import scala.of.coq.parsercombinators.parser.CoqParser
import scala.of.coq.parsercombinators.compiler.ScalaOfCoq
import scala.of.coq.parsercombinators.compiler.NoCurrying
import scala.of.coq.parsercombinators.compiler.Currify

object Main {
  val usage = """
    Usage: scala target/scala-2.11/scallina-assembly-<scallina-version>.jar [--uncurrify] [--source] [--trim] [--ast] [--lexer] [--coq] <coq-source-file-1.v> ... <coq-source-file-n.v>
  """

  def printUsageAndExit(exitCode: Int): Unit = {
    println(usage)
    System.exit(exitCode)
  }

  type OptionMap = Map[Symbol, Boolean]
  type FileNames = List[String]

  val source = Symbol("source")
  val trim = Symbol("trim")
  val ast = Symbol("ast")
  val lexer = Symbol("lexer")
  val coq = Symbol("coq")
  val uncurrify = Symbol("uncurrify")

  val defaultMap: OptionMap = Map[Symbol, Boolean]() withDefaultValue false
  def parseCommandLineArgs(commandLineArgs: List[String]): (OptionMap, FileNames) = {

    @tailrec
    def nextArgument(optionMap: OptionMap, fileNamesAcc: FileNames, commandLineArgs: List[String]): (OptionMap, FileNames) = {
      commandLineArgs match {
        case "--source" :: tail    => nextArgument(optionMap ++ Map(source -> true), fileNamesAcc, tail)
        case "--trim" :: tail      => nextArgument(optionMap ++ Map(trim -> true), fileNamesAcc, tail)
        case "--ast" :: tail       => nextArgument(optionMap ++ Map(ast -> true), fileNamesAcc, tail)
        case "--lexer" :: tail     => nextArgument(optionMap ++ Map(lexer -> true), fileNamesAcc, tail)
        case "--coq" :: tail       => nextArgument(optionMap ++ Map(coq -> true), fileNamesAcc, tail)
        case "--uncurrify" :: tail => nextArgument(optionMap ++ Map(uncurrify -> true), fileNamesAcc, tail)
        case fileName :: tail      => nextArgument(optionMap, fileName :: fileNamesAcc, tail)
        case Nil                   => (optionMap, fileNamesAcc)
      }
    }

    nextArgument(defaultMap, Nil, commandLineArgs)
  }

  def main(args: Array[String]) : Unit = {
    if (args.length <= 0) {
      printUsageAndExit(1)
    }
    val arglist = args.toList

    try {
      val (map, fileNames) = parseCommandLineArgs(arglist)

      for (fileName <- fileNames) {
        val fileBufferedSource = io.Source.fromFile(fileName)
        val inputString = try fileBufferedSource.mkString finally fileBufferedSource.close()

        val shouldTrimCode = map(trim)

        if (map(source)) {
          println("Before parsing:")
          println(if (shouldTrimCode) inputString.trim else inputString)
          println("After parsing:")
        }

        val coqAST = CoqParser(inputString)
        val optionalCoqAst = Option(coqAST.getOrElse(null))

        val outputString = optionalCoqAst.fold(coqAST.toString) {
          coqTrees =>
            val shouldPrintCoqOutput = map(coq)
            if (shouldPrintCoqOutput) {
              coqTrees.map(_.toCoqCode).mkString("\n")
            } else {
              val scalaOfCoq = new ScalaOfCoq(coqTrees,
                if (map(uncurrify))
                  NoCurrying
                else
                  Currify
              )
              scalaOfCoq.createObjectFileCode(
                fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."))
              )
            }
        }

        println(if (shouldTrimCode) outputString.trim else outputString)

        if (map(ast)) {
          println(coqAST)
        }

        if (map(lexer)) {
          println(CoqLexer.parseAllTokens(inputString))
        }

        println()

      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        printUsageAndExit(2)
    }
  }
}

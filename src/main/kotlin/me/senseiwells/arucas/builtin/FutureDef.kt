package me.senseiwells.arucas.builtin

import me.senseiwells.arucas.api.docs.ClassDoc
import me.senseiwells.arucas.api.docs.FunctionDoc
import me.senseiwells.arucas.classes.ClassInstance
import me.senseiwells.arucas.classes.CreatableDefinition
import me.senseiwells.arucas.core.Interpreter
import me.senseiwells.arucas.utils.Arguments
import me.senseiwells.arucas.utils.BuiltInFunction
import me.senseiwells.arucas.utils.MemberFunction
import me.senseiwells.arucas.utils.Util.Types.BOOLEAN
import me.senseiwells.arucas.utils.Util.Types.FUTURE
import me.senseiwells.arucas.utils.Util.Types.OBJECT
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@ClassDoc(
    name = FUTURE,
    desc = [
        "This class is used to represent values that are in the future.",
        "More precisely values that are being evaluated on another thread,",
        "this allows you to access those values once they've been processed"
    ]
)
class FutureDef(interpreter: Interpreter): CreatableDefinition<Future<*>>(FUTURE, interpreter) {
    override fun canExtend() = false

    override fun defineStaticMethods(): List<BuiltInFunction> {
        return listOf(
            BuiltInFunction.of("completed", 1, this::completed)
        )
    }

    @FunctionDoc(
        isStatic = true,
        name = "completed",
        desc = ["This returns a future that with a complete value"],
        params = [OBJECT, "value", "The value to complete the future with"],
        returns = [FUTURE, "The future that has been completed with the value"],
        examples = ["future = Future.completed(true);"]
    )
    private fun completed(arguments: Arguments): ClassInstance {
        return this.create(CompletableFuture.completedFuture(arguments.next()))
    }

    override fun defineMethods(): List<MemberFunction> {
        return listOf(
            MemberFunction.of("await", this::await),
            MemberFunction.of("isComplete", this::isComplete)
        )
    }

    @FunctionDoc(
        name = "await",
        desc = [
            "This blocks the current thread until the future has",
            "been completed and then returns the value of the future"
        ],
        returns = [OBJECT, "The value of the future"],
        examples = ["future.await();"]
    )
    private fun await(arguments: Arguments): Any? {
        return arguments.interpreter.interuptable {
            arguments.nextPrimitive(this).get()
        }
    }

    @FunctionDoc(
        name = "isComplete",
        desc = ["This returns whether the future has been completed"],
        returns = [BOOLEAN, "Whether the future has been completed"],
        examples = ["future.isComplete();"]
    )
    private fun isComplete(arguments: Arguments): Boolean {
        return arguments.nextPrimitive(this).isDone
    }
}
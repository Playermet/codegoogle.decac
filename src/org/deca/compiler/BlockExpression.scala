package org.deca.compiler

import org.jllvm.LLVMValue
import org.jllvm.LLVMBasicBlock
import org.jllvm.LLVMFunction
import org.jllvm.LLVMInstructionBuilder
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

class BlockExpression(val steps: List[Expression]) extends Expression {
  expType = steps.last.expType
  expEffect = ExpressionEffect(new EffectVariable,new EffectVariable)
  override val children = steps
  
  override def constrain(scs: SignatureConstraints): Unit =
    for(step <- steps) {
      scs.push(new SubsumptionConstraint(step.expEffect.positive,expEffect.positive))
      scs.push(new SubsumptionConstraint(step.expEffect.negative,expEffect.negative))
    }
  override def check(scs: SignatureConstraints): Unit = assert(expEffect.safe(PureEffect))
  override def substitute(sub: SignatureSubstitution): Unit = {
    for(step <- steps)
      step.substitute(sub)
    expType = sub.solve(expType).asInstanceOf[MonoType]
  }
  override def specialize(spec: SignatureSubstitution): Expression =
    new BlockExpression(steps.map(_.specialize(spec)))
  override def compile(builder: LLVMInstructionBuilder,scope: Scope,instantiation: Module): LLVMValue = 
    steps.map(_.compile(builder,scope,instantiation)).last
}

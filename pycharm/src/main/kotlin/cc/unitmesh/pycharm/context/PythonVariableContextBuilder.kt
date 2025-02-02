package cc.unitmesh.pycharm.context

import cc.unitmesh.devti.context.VariableContext
import cc.unitmesh.devti.context.builder.VariableContextBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Query
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyTargetExpression

class PythonVariableContextBuilder : VariableContextBuilder {
    override fun getVariableContext(
        psiElement: PsiElement,
        includeMethodContext: Boolean,
        includeClassContext: Boolean,
        gatherUsages: Boolean
    ): VariableContext? {
        return when (psiElement) {
            is PyTargetExpression -> {
                val text = psiElement.text
                val name = psiElement.name
                val enclosingMethod = PsiTreeUtil.getParentOfType(psiElement, PyFunction::class.java, true)
                val enclosingClass = psiElement.containingClass
                val usages =
                    if (gatherUsages) findUsages(psiElement as PsiNameIdentifierOwner) else emptyList()
                VariableContext(
                    psiElement,
                    text,
                    name,
                    enclosingMethod,
                    enclosingClass,
                    usages,
                    includeMethodContext,
                    includeClassContext
                )
            }

            is PyNamedParameter -> {
                val text = psiElement.text
                val name = psiElement.name
                val enclosingMethod = PsiTreeUtil.getParentOfType(psiElement, PyFunction::class.java, true)
                val enclosingClass = PsiTreeUtil.getParentOfType(
                    psiElement,
                    PyFunction::class.java,
                    true
                )?.containingClass as PsiElement?
                val usages =
                    if (gatherUsages) findUsages(psiElement as PsiNameIdentifierOwner) else emptyList()
                VariableContext(
                    psiElement,
                    text,
                    name,
                    enclosingMethod,
                    enclosingClass,
                    usages,
                    includeMethodContext,
                    includeClassContext
                )
            }

            else -> null
        }
    }

    companion object {
        fun findUsages(nameIdentifierOwner: PsiNameIdentifierOwner): List<PsiReference> {
            val project = nameIdentifierOwner.project
            val searchScope = GlobalSearchScope.allScope(project)
            val query: Query<PsiReference> =
                ReferencesSearch.search(nameIdentifierOwner as PsiElement, searchScope, true)
            val results: Collection<PsiReference> = query.findAll()
            return ArrayList(results)
        }
    }

}

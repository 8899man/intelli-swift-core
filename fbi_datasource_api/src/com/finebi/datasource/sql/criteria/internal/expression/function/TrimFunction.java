
package com.finebi.datasource.sql.criteria.internal.expression.function;

import com.finebi.datasource.api.criteria.CriteriaBuilder.Trimspec;
import com.finebi.datasource.api.criteria.Expression;
import com.finebi.datasource.sql.criteria.internal.CriteriaBuilderImpl;
import com.finebi.datasource.sql.criteria.internal.ParameterContainer;
import com.finebi.datasource.sql.criteria.internal.ParameterRegistry;
import com.finebi.datasource.sql.criteria.internal.Renderable;
import com.finebi.datasource.sql.criteria.internal.compile.RenderingContext;
import com.finebi.datasource.sql.criteria.internal.expression.LiteralExpression;

import java.io.Serializable;

/**
 * Models the ANSI SQL <tt>TRIM</tt> function.
 *
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public class TrimFunction
        extends BasicFunctionExpression<String>
        implements Serializable {
    public static final String NAME = "trim";
    public static final Trimspec DEFAULT_TRIMSPEC = Trimspec.BOTH;
    public static final char DEFAULT_TRIM_CHAR = ' ';

    private final Trimspec trimspec;
    private final Expression<Character> trimCharacter;
    private final Expression<String> trimSource;

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            Trimspec trimspec,
            Expression<Character> trimCharacter,
            Expression<String> trimSource) {
        super(criteriaBuilder, String.class, NAME);
        this.trimspec = trimspec;
        this.trimCharacter = trimCharacter;
        this.trimSource = trimSource;
    }

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            Trimspec trimspec,
            char trimCharacter,
            Expression<String> trimSource) {
        super(criteriaBuilder, String.class, NAME);
        this.trimspec = trimspec;
        this.trimCharacter = new LiteralExpression<Character>(criteriaBuilder, trimCharacter);
        this.trimSource = trimSource;
    }

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, DEFAULT_TRIM_CHAR, trimSource);
    }

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            Expression<Character> trimCharacter,
            Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, trimCharacter, trimSource);
    }

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            char trimCharacter,
            Expression<String> trimSource) {
        this(criteriaBuilder, DEFAULT_TRIMSPEC, trimCharacter, trimSource);
    }

    public TrimFunction(
            CriteriaBuilderImpl criteriaBuilder,
            Trimspec trimspec,
            Expression<String> trimSource) {
        this(criteriaBuilder, trimspec, DEFAULT_TRIM_CHAR, trimSource);
    }

    public Expression<Character> getTrimCharacter() {
        return trimCharacter;
    }

    public Expression<String> getTrimSource() {
        return trimSource;
    }

    public Trimspec getTrimspec() {
        return trimspec;
    }

    @Override
    public void registerParameters(ParameterRegistry registry) {
        ParameterContainer.Helper.possibleParameter(getTrimCharacter(), registry);
        ParameterContainer.Helper.possibleParameter(getTrimSource(), registry);
    }

    @Override
    public String render(RenderingContext renderingContext) {
        String renderedTrimChar;
        if (trimCharacter.getClass().isAssignableFrom(
                LiteralExpression.class)) {
            // If the character is a literal, treat it as one.  A few dialects
            // do not support parameters as trim() arguments.
            renderedTrimChar = ((LiteralExpression<Character>)
                    trimCharacter).getLiteral().toString();
        } else {
            renderedTrimChar = ((Renderable) trimCharacter).render(
                    renderingContext).toString();
        }
        return new StringBuilder()
                .append("trim(")
                .append(trimspec.name())
                .append(' ')
                .append(renderedTrimChar)
                .append(" from ")
                .append(((Renderable) trimSource).render(renderingContext))
                .append(')')
                .toString();
    }
}

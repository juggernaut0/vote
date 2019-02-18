package vote.db.jooq

import java.sql.*

import org.jooq.*
import org.jooq.conf.ParamType
import org.jooq.impl.DSL

class PostgresJSONBinding : Binding<Any, String> {
    override fun converter(): Converter<Any, String> {
        return object : Converter<Any, String> {
            override fun from(t: Any?): String? {
                return t?.toString()
            }

            override fun to(u: String?): Any? {
                return u
            }

            override fun fromType(): Class<Any> {
                return Any::class.java
            }

            override fun toType(): Class<String> {
                return String::class.java
            }
        }
    }

    // Rending a bind variable for the binding context's value and casting it to the json type
    override fun sql(ctx: BindingSQLContext<String>) {
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json")
        else
            ctx.render().sql("?::json")
    }

    override fun register(ctx: BindingRegisterContext<String>) {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR)
    }

    // Converting the JsonElement to a String value and setting that on a JDBC PreparedStatement
    override fun set(ctx: BindingSetStatementContext<String>) {
        ctx.statement().setString(ctx.index(), ctx.convert(converter()).value().toString())
    }

    // Getting a String value from a JDBC ResultSet and converting that to a JsonElement
    override fun get(ctx: BindingGetResultSetContext<String>) {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()))
    }

    // Getting a String value from a JDBC CallableStatement and converting that to a JsonElement
    override fun get(ctx: BindingGetStatementContext<String>) {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()))
    }

    // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
    override fun set(ctx: BindingSetSQLOutputContext<String>) {
        throw SQLFeatureNotSupportedException()
    }

    // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
    override fun get(ctx: BindingGetSQLInputContext<String>) {
        throw SQLFeatureNotSupportedException()
    }
}
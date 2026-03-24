package Quantum;

import java.util.Locale;

final class ExpressionParser {
    private final String input;
    private int position;

    ExpressionParser(String input) {
        this.input = input.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    Expression parse() {
        Expression expression = parseExpression();
        if (position != input.length()) {
            throw new IllegalArgumentException("Unexpected token at position " + (position + 1));
        }
        return expression;
    }

    private Expression parseExpression() {
        Expression left = parseTerm();
        while (match('+') || match('-')) {
            char operator = input.charAt(position - 1);
            Expression right = parseTerm();
            Expression currentLeft = left;
            left = operator == '+'
                    ? x -> currentLeft.evaluate(x) + right.evaluate(x)
                    : x -> currentLeft.evaluate(x) - right.evaluate(x);
        }
        return left;
    }

    private Expression parseTerm() {
        Expression left = parsePower();
        while (match('*') || match('/')) {
            char operator = input.charAt(position - 1);
            Expression right = parsePower();
            Expression currentLeft = left;
            left = operator == '*'
                    ? x -> currentLeft.evaluate(x) * right.evaluate(x)
                    : x -> currentLeft.evaluate(x) / right.evaluate(x);
        }
        return left;
    }

    private Expression parsePower() {
        Expression base = parseUnary();
        if (match('^')) {
            Expression exponent = parsePower();
            return x -> Math.pow(base.evaluate(x), exponent.evaluate(x));
        }
        return base;
    }

    private Expression parseUnary() {
        if (match('+')) {
            return parseUnary();
        }
        if (match('-')) {
            Expression nested = parseUnary();
            return x -> -nested.evaluate(x);
        }
        return parsePrimary();
    }

    private Expression parsePrimary() {
        if (match('(')) {
            Expression expression = parseExpression();
            expect(')');
            return expression;
        }

        if (peekLetter()) {
            String identifier = parseIdentifier();
            if ("x".equals(identifier)) {
                return x -> x;
            }
            if ("pi".equals(identifier)) {
                return x -> Math.PI;
            }
            if ("e".equals(identifier)) {
                return x -> Math.E;
            }

            expect('(');
            Expression argument = parseExpression();
            expect(')');
            return buildFunction(identifier, argument);
        }

        return parseNumber();
    }

    private Expression buildFunction(String name, Expression argument) {
        return switch (name) {
            case "sin" -> x -> Math.sin(argument.evaluate(x));
            case "cos" -> x -> Math.cos(argument.evaluate(x));
            case "tan" -> x -> Math.tan(argument.evaluate(x));
            case "sqrt" -> x -> Math.sqrt(argument.evaluate(x));
            case "log" -> x -> Math.log10(argument.evaluate(x));
            case "ln" -> x -> Math.log(argument.evaluate(x));
            case "abs" -> x -> Math.abs(argument.evaluate(x));
            case "diff" -> x -> MathSupport.approximateDerivative(argument, x);
            case "int" -> x -> MathSupport.approximateIntegral(argument, 0.0, x);
            default -> throw new IllegalArgumentException("Unknown function: " + name);
        };
    }

    private Expression parseNumber() {
        int start = position;
        while (position < input.length()
                && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
            position++;
        }

        if (start == position) {
            throw new IllegalArgumentException("Expected number or expression at position " + (position + 1));
        }

        double value = Double.parseDouble(input.substring(start, position));
        return x -> value;
    }

    private String parseIdentifier() {
        int start = position;
        while (position < input.length() && Character.isLetter(input.charAt(position))) {
            position++;
        }
        return input.substring(start, position);
    }

    private void expect(char expected) {
        if (!match(expected)) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + (position + 1));
        }
    }

    private boolean match(char expected) {
        if (position < input.length() && input.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private boolean peekLetter() {
        return position < input.length() && Character.isLetter(input.charAt(position));
    }

    @FunctionalInterface
    interface Expression {
        double evaluate(double x);
    }
}

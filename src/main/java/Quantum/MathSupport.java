package Quantum;

final class MathSupport {
    private MathSupport() {
    }

    static double approximateDerivative(ExpressionParser.Expression expression, double x) {
        double step = Math.max(1e-5, Math.abs(x) * 1e-5);
        double left = evaluateOrThrow(expression, x - step);
        double right = evaluateOrThrow(expression, x + step);
        return (right - left) / (2.0 * step);
    }

    static double approximateIntegral(ExpressionParser.Expression expression, double lowerBound, double upperBound) {
        if (Math.abs(upperBound - lowerBound) < 1e-12) {
            return 0.0;
        }

        double start = lowerBound;
        double end = upperBound;
        double sign = 1.0;
        if (upperBound < lowerBound) {
            start = upperBound;
            end = lowerBound;
            sign = -1.0;
        }

        int intervals = 2000;
        double step = (end - start) / intervals;
        double sum = evaluateOrThrow(expression, start) + evaluateOrThrow(expression, end);

        for (int index = 1; index < intervals; index++) {
            double x = start + index * step;
            double value = evaluateOrThrow(expression, x);
            sum += (index % 2 == 0 ? 2.0 : 4.0) * value;
        }

        return sign * (step / 3.0) * sum;
    }

    static double evaluateOrThrow(ExpressionParser.Expression expression, double x) {
        double value;
        try {
            value = expression.evaluate(x);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("The function could not be evaluated in the requested range.");
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("The function could not be evaluated in the requested range.");
        }
        return value;
    }
}

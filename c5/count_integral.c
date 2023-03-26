double integrate(double (*func)(double), double begin, double end, int num_points)
{
    double dx = (end - begin) / (double)(num_points - 1);
    double sum = 0.0;
    for (int i = 1; i < num_points - 1; i++)
    {
        double x = begin + i * dx;
        sum += func(x);
    }
    sum += (func(begin) + func(end)) / 2.0;
    return sum * dx;
}


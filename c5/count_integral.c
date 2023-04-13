double integrate(double (*func)(double), double begin, double end, int num_points)
{
    double h = (end - begin) / num_points;
    double sum = 0;
    for (int i = 0; i < num_points; i++)
    {
        sum += (func(begin + i * h) +  func(begin + (i+1) * h)) / 2;
    }
    return h * sum;
}
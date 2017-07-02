package com.bluetrainsoftware.test.web;

import javax.servlet.annotation.WebFilter;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@WebFilter(filterName = "peanuts", urlPatterns = {"/nuts/*", "groundnuts/*"})
public class Sample2Filter {
}

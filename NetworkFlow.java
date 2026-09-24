package src;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class NetworkFlow {

    static int vertices;

    static boolean bfs(int[][] residual, int source, int sink, int[] parent) {

        boolean[] visited = new boolean[vertices];
        Queue<Integer> queue = new LinkedList<>();

        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {

            int current = queue.poll();

            for (int next = 0; next < vertices; next++) {

                if (!visited[next] && residual[current][next] > 0) {

                    queue.add(next);
                    parent[next] = current;
                    visited[next] = true;

                    if (next == sink) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static int maxFlow(int[][] graph, int source, int sink) {

        vertices = graph.length;

        int[][] residual = new int[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                residual[i][j] = graph[i][j];
            }
        }

        int[] parent = new int[vertices];
        int maximumFlow = 0;

        while (bfs(residual, source, sink, parent)) {

            int pathFlow = Integer.MAX_VALUE;
            int current = sink;

            while (current != source) {

                int previous = parent[current];

                pathFlow = Math.min(
                        pathFlow,
                        residual[previous][current]
                );

                current = previous;
            }

            current = sink;

            while (current != source) {

                int previous = parent[current];

                residual[previous][current] -= pathFlow;
                residual[current][previous] += pathFlow;

                current = previous;
            }

            maximumFlow += pathFlow;
        }

        return maximumFlow;
    }

    static class Recipe {
        String name;
        String text;

        Recipe(String name, String text) {
            this.name = name;
            this.text = text;
        }
    }

    static List<Recipe> loadRecipes() {

        List<Recipe> recipes = new ArrayList<>();

        Path corpusPath = Paths.get("corpus");

        try {

            if (!Files.exists(corpusPath)) {
                System.out.println("Corpus folder not found.");
                return recipes;
            }

            DirectoryStream<Path> files =
                    Files.newDirectoryStream(corpusPath, "*.txt");

            for (Path file : files) {

                String text = Files.readString(file);

                String[] parts =
                        text.split("(?=RECIPE:)");

                for (String recipeText : parts) {

                    recipeText = recipeText.trim();

                    if (recipeText.isEmpty()) {
                        continue;
                    }

                    String recipeName = "Unknown Recipe";

                    for (String line : recipeText.split("\\R")) {

                        line = line.trim();

                        if (line.startsWith("RECIPE:")) {
                            recipeName =
                                    line.substring(7).trim();
                            break;
                        }
                    }

                    recipes.add(
                            new Recipe(recipeName, recipeText)
                    );
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading corpus.");
        }

        return recipes;
    }

    static List<Recipe> findMatches(
            List<Recipe> recipes,
            String keyword) {

        List<Recipe> matches = new ArrayList<>();

        keyword = keyword.toLowerCase().trim();

        for (Recipe recipe : recipes) {

            if (recipe.text.toLowerCase().contains(keyword)) {
                matches.add(recipe);
            }
        }

        return matches;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<Recipe> recipes = loadRecipes();

        if (recipes.isEmpty()) {
            System.out.println("No recipes found.");
            return;
        }

        System.out.println("============= RECIPE NETWORK FLOW =============");
        System.out.println();

        System.out.print("Enter ingredient or keyword: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Please enter a keyword.");
            return;
        }

        List<Recipe> matches =
                findMatches(recipes, keyword);

        int matchingRecipes = matches.size();

        System.out.println();
        System.out.println("Source : " + keyword.toUpperCase());
        System.out.println();

        System.out.println(keyword.toUpperCase());
        System.out.println("  |");

        for (int i = 0; i < matchingRecipes; i++) {

            System.out.println(
                    "  +--> " + matches.get(i).name + " (1)"
            );
        }

        System.out.println("        |");
        System.out.println("        v");
        System.out.println("   Recipe Database");
        System.out.println("        |");
        System.out.println("        v");
        System.out.println("   Search Results");

        System.out.println();
        System.out.println("-----------------------------------------------");

        System.out.println(
                "Matching Recipes : " + matchingRecipes
        );

        int source = 0;
        int recipeStart = 1;
        int database = matchingRecipes + 1;
        int results = matchingRecipes + 2;
        int sink = matchingRecipes + 3;

        int totalVertices = matchingRecipes + 4;

        int[][] graph =
                new int[totalVertices][totalVertices];

        for (int i = 0; i < matchingRecipes; i++) {

            int recipeNode = recipeStart + i;

            graph[source][recipeNode] = 1;
            graph[recipeNode][database] = 1;
        }

        graph[database][results] = matchingRecipes;
        graph[results][sink] = matchingRecipes;

        int maximumFlow =
                maxFlow(graph, source, sink);

        System.out.println(
                "Maximum Flow     : " + maximumFlow
        );

        System.out.println("-----------------------------------------------");

        scanner.close();
    }
}

use yew::prelude::*;
use reqwest;
use serde::Deserialize;

#[derive(Clone, PartialEq, Deserialize, Debug)]
struct Meal {
    idMeal: String,
    strMeal: String,
    strCategory: String,
    strArea: String,
    strMealThumb: String,
    strYoutube: Option<String>,
}

#[derive(Deserialize, Debug)]
struct MealResponse {
    meals: Option<Vec<Meal>>,
}

#[function_component(App)]
fn app() -> Html {
    let meals = use_state(|| None::<Vec<Meal>>);
    let loading = use_state(|| true);
    let error = use_state(|| None::<String>);

    {
        let meals = meals.clone();
        let loading = loading.clone();
        let error = error.clone();

        use_effect_with((), move |_| {
            wasm_bindgen_futures::spawn_local(async move {
                let url = "https://www.themealdb.com/api/json/v1/1/search.php?f=a";
                match reqwest::get(url).await {
                    Ok(resp) => {
                        if let Ok(json) = resp.json::<MealResponse>().await {
                            meals.set(json.meals.unwrap_or_default());
                        } else {
                            error.set(Some("Failed to parse the response".into()));
                        }
                    }
                    Err(err) => {
                        error.set(Some(format!("Fetch error: {}", err)));
                    }
                }
                loading.set(false);
            });
            || ()
        });
    }

    let render_meals = {
        if *loading {
            html! {
                <div class="loader">
                    <div class="spinner"></div>
                </div>
            }
        } else if let Some(err) = &*error {
            html! {
                <p style="text-align: center; color: #ef4444;">{ err }</p>
            }
        } else if let Some(meals_list) = &*meals {
            if meals_list.is_empty() {
                html! { <p style="text-align: center; color: var(--text-secondary);">{"No meals found."}</p> }
            } else {
                html! {
                    <div class="meals-grid">
                        {
                            for meals_list.iter().map(|meal| {
                                html! {
                                    <article class="meal-card">
                                        <img src={meal.strMealThumb.clone()} alt={meal.strMeal.clone()} class="meal-img" loading="lazy" />
                                        <div class="meal-info">
                                            <h3 class="meal-title">{ &meal.strMeal }</h3>
                                            <div class="meal-tags">
                                                <span class="tag">{ &meal.strCategory }</span>
                                                <span class="tag area">{ &meal.strArea }</span>
                                            </div>
                                            if let Some(yt) = &meal.strYoutube {
                                                if !yt.is_empty() {
                                                    <a href={yt.clone()} target="_blank" rel="noopener" class="meal-btn">{"Watch Recipe"}</a>
                                                }
                                            }
                                        </div>
                                    </article>
                                }
                            })
                        }
                    </div>
                }
            }
        } else {
            html! {}
        }
    };

    html! {
        <div class="app-container">
            <header class="main-header">
                <h1>{ "TheMealDB Rust App" }</h1>
                <p>{ "Discover delicious recipes powered by WebAssembly" }</p>
            </header>
            <main>
                { render_meals }
            </main>
        </div>
    }
}

fn main() {
    yew::Renderer::<App>::new().render();
}
